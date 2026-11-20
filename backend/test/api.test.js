const { test } = require("node:test");
const assert = require("node:assert/strict");
const { createApp } = require("../src/app");
const { calculateAvailability } = require("../src/services/availabilityCalculator");

test("calculateAvailability excludes no-telemetry periods and follows official formula", () => {
  const res = calculateAvailability({
    totalPeriodSeconds: 1000,
    downtimeSeconds: 50,
    noTelemetrySeconds: 500,
  });
  // Observed = 1000 - 500 = 500s; Availability = ((500 - 50) / 500) * 100 = 90.00%
  assert.equal(res.availabilityPct, 90);
  assert.equal(res.hasTelemetry, true);

  const noData = calculateAvailability({
    totalPeriodSeconds: 1000,
    downtimeSeconds: 0,
    noTelemetrySeconds: 1000,
  });
  assert.equal(noData.availabilityPct, 0);
  assert.equal(noData.hasTelemetry, false);
});

test("REST API authenticates collector & client, detects 3-failure drop and 2-success recovery", async () => {
  const { server } = createApp({
    collectorToken: "test-collector-secret",
    verifyClientToken: async (token) => token === "valid-user-jwt",
  });

  await new Promise((resolve) => server.listen(0, "127.0.0.1", resolve));
  const { port } = server.address();
  const baseUrl = `http://127.0.0.1:${port}`;

  try {
    // Unauthenticated heartbeat rejected
    const unauth = await fetch(`${baseUrl}/api/monitoring/heartbeat`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ collectorId: "c1" }),
    });
    assert.equal(unauth.status, 401);

    // Authorized heartbeat succeeds
    const hb = await fetch(`${baseUrl}/api/monitoring/heartbeat`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: "Bearer test-collector-secret",
      },
      body: JSON.stringify({ collectorId: "c1", agenciesCount: 7 }),
    });
    assert.equal(hb.status, 200);

    // 3 consecutive failed ICMP checks trigger RED + CRITICAL alert
    for (let i = 0; i < 3; i++) {
      await fetch(`${baseUrl}/api/monitoring/metrics`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: "Bearer test-collector-secret",
        },
        body: JSON.stringify({
          agencyId: "calderon",
          linkMetric: { reachable: false, intervalSec: 30 },
        }),
      });
    }

    const dash1 = await fetch(`${baseUrl}/api/dashboard`, {
      headers: { Authorization: "Bearer valid-user-jwt" },
    }).then((r) => r.json());
    assert.equal(dash1.downInternetLinks, 1);
    assert.equal(dash1.totalAgencies, 7);

    // 2 consecutive successful checks recover link to GREEN
    for (let i = 0; i < 2; i++) {
      await fetch(`${baseUrl}/api/monitoring/metrics`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: "Bearer test-collector-secret",
        },
        body: JSON.stringify({
          agencyId: "calderon",
          linkMetric: { reachable: true, latencyMs: 18, packetLossPct: 0, intervalSec: 30 },
        }),
      });
    }

    const dash2 = await fetch(`${baseUrl}/api/dashboard`, {
      headers: { Authorization: "Bearer valid-user-jwt" },
    }).then((r) => r.json());
    assert.equal(dash2.downInternetLinks, 0);
    assert.equal(dash2.activeInternetLinks, 1);
  } finally {
    server.close();
  }
});
