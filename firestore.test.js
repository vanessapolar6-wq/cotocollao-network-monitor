const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_noc_123";
const BOB_UID = "bob_noc_456";
const ADMIN_EMAIL = "vanessapolar6@gmail.com";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function pastDate(offsetMs = 5000) {
  return new Date(Date.now() - offsetMs);
}

function validAgencyPayload(uid, name = "MATRIZ") {
  const ts = pastDate();
  return {
    userId: uid,
    code: "AG-01",
    name,
    cityZone: "Quito Norte",
    publicIp: "",
    gatewayIp: "",
    switchIp: "",
    firewallIp: "",
    ispProvider: "",
    contractedBandwidthMbps: 0,
    status: "GRAY",
    linkStatus: "GRAY",
    switchStatus: "GRAY",
    firewallStatus: "GRAY",
    latencyMs: 0,
    packetLossPct: 0,
    availabilityPct: 0,
    downtimeSeconds: 0,
    noTelemetrySeconds: 0,
    lastCheckEpochMs: 0,
    lastConnectionEpochMs: 0,
    configured: false,
    createdAt: ts,
    updatedAt: ts,
  };
}

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user cannot read or write any workspace data", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("agencies").get());
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("agencies").doc("matriz").set(validAgencyPayload(ALICE_UID))
  );
});

test("User profile creation enforces anti-escalation and email spoofing guard", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID, {
    email: "alice@cotocollao.fin.ec",
    email_verified: true,
  }).firestore();
  const ts = pastDate();

  // Normal user cannot self-assign ADMIN
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Alice",
      email: "alice@cotocollao.fin.ec",
      role: "ADMIN",
      active: true,
      createdAt: ts,
      updatedAt: ts,
    })
  );

  // Normal user can register with MANAGER role
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Alice",
      email: "alice@cotocollao.fin.ec",
      role: "MANAGER",
      active: true,
      createdAt: ts,
      updatedAt: ts,
    })
  );

  // Normal user cannot escalate own role via update
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      role: "ADMIN",
      updatedAt: pastDate(1000),
    })
  );

  // Spoofed admin email with email_verified=false must fail
  const spoofDb = testEnv.authenticatedContext("spoof_uid", {
    email: ADMIN_EMAIL,
    email_verified: false,
  }).firestore();
  await assertFails(
    spoofDb.collection("users").doc("spoof_uid").set({
      userId: "spoof_uid",
      displayName: "Spoof",
      email: ADMIN_EMAIL,
      role: "ADMIN",
      active: true,
      createdAt: ts,
      updatedAt: ts,
    })
  );

  // Verified bootstrapped admin email succeeds with ADMIN role
  const adminDb = testEnv.authenticatedContext("admin_uid", {
    email: ADMIN_EMAIL,
    email_verified: true,
  }).firestore();
  await assertSucceeds(
    adminDb.collection("users").doc("admin_uid").set({
      userId: "admin_uid",
      displayName: "Vanessa Polar",
      email: ADMIN_EMAIL,
      role: "ADMIN",
      active: true,
      createdAt: ts,
      updatedAt: ts,
    })
  );
});

test("Agencies: owner CRUD succeeds, @DocumentId 'id' field & ghost fields fail, cross-user fails", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("agencies").doc("matriz").set(validAgencyPayload(ALICE_UID))
  );

  // List own agencies succeeds (even when matching Kotlin query)
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).collection("agencies").get());

  // Cross-user read/write must fail
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("agencies").get());
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("agencies").doc("matriz").update({
      name: "Hacked",
      updatedAt: pastDate(1000),
    })
  );

  // Reject ghost field / @DocumentId 'id' inside body
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("agencies").doc("calderon").set({
      ...validAgencyPayload(ALICE_UID, "CALDERÓN"),
      id: "calderon",
    })
  );

  // Reject tampered future timestamp
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("agencies").doc("calderon").set({
      ...validAgencyPayload(ALICE_UID, "CALDERÓN"),
      createdAt: new Date(Date.now() + 60000),
    })
  );

  // Valid partial update succeeds
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("agencies").doc("matriz").update({
      publicIp: "200.10.20.30",
      ispProvider: "Telconet",
      contractedBandwidthMbps: 200,
      configured: true,
      updatedAt: pastDate(1000),
    })
  );
});

test("Devices & Incidents: relational agency existence, terminal CLOSED lock, and immutable audit logs", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const ts = pastDate();

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("agencies").doc("matriz").set(validAgencyPayload(ALICE_UID))
  );

  // Device referencing non-existent agency must fail
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("devices").doc("sw_missing").set({
      userId: ALICE_UID,
      agencyId: "missing_agency",
      agencyName: "UNKNOWN",
      deviceType: "SWITCH_L3",
      hostname: "SW-CORE-01",
      ipAddress: "10.10.1.2",
      brand: "Cisco",
      model: "Catalyst 9300",
      serialNumber: "SN123",
      firmwareVersion: "17.9",
      protocol: "SNMPV3",
      secretRefId: "vault://snmpv3/matriz-sw1",
      status: "GRAY",
      cpuPct: 0,
      ramPct: 0,
      tempCelsius: 0,
      uptimeSeconds: 0,
      activeInterfaces: 0,
      totalInterfaces: 24,
      txErrors: 0,
      droppedPackets: 0,
      vpnTunnelsUp: 0,
      vpnTunnelsTotal: 0,
      lastCheckEpochMs: 0,
      createdAt: ts,
      updatedAt: ts,
    })
  );

  // Valid device referencing existing agency succeeds
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("devices").doc("sw_matriz").set({
      userId: ALICE_UID,
      agencyId: "matriz",
      agencyName: "MATRIZ",
      deviceType: "SWITCH_L3",
      hostname: "SW-CORE-01",
      ipAddress: "10.10.1.2",
      brand: "Cisco",
      model: "Catalyst 9300",
      serialNumber: "SN123",
      firmwareVersion: "17.9",
      protocol: "SNMPV3",
      secretRefId: "vault://snmpv3/matriz-sw1",
      status: "GRAY",
      cpuPct: 0,
      ramPct: 0,
      tempCelsius: 0,
      uptimeSeconds: 0,
      activeInterfaces: 0,
      totalInterfaces: 24,
      txErrors: 0,
      droppedPackets: 0,
      vpnTunnelsUp: 0,
      vpnTunnelsTotal: 0,
      lastCheckEpochMs: 0,
      createdAt: ts,
      updatedAt: ts,
    })
  );

  // Incident create & transition to CLOSED
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("incidents").doc("inc_1").set({
      userId: ALICE_UID,
      code: "INC-2026-001",
      agencyId: "matriz",
      agencyName: "MATRIZ",
      deviceId: "sw_matriz",
      deviceName: "SW-CORE-01",
      deviceType: "SWITCH_L3",
      failureType: "PERDIDA_SNMP",
      severity: "CRITICAL",
      status: "NEW",
      startedAtEpochMs: 1700000000000,
      recoveredAtEpochMs: 0,
      durationSeconds: 0,
      assignedTo: "Tecnico NOC",
      diagnosis: "",
      solution: "",
      observations: "",
      evidenceNote: "",
      createdAt: ts,
      updatedAt: ts,
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("incidents").doc("inc_1").update({
      status: "CLOSED",
      solution: "Reinicio controlado de modulo SNMPv3",
      updatedAt: pastDate(1000),
    })
  );

  // Non-admin cannot modify a CLOSED incident
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("incidents").doc("inc_1").update({
      status: "IN_PROGRESS",
      updatedAt: pastDate(500),
    })
  );

  // Audit log is create-only, immutable
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("audit_logs").doc("log_1").set({
      userId: ALICE_UID,
      actorEmail: "alice@cotocollao.fin.ec",
      actorRole: "MANAGER",
      action: "CREATE_INCIDENT",
      target: "INC-2026-001",
      details: "Incident logged",
      timestampEpochMs: 1700000000000,
      createdAt: ts,
    })
  );
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("audit_logs").doc("log_1").update({
      details: "Tampered",
    })
  );
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("audit_logs").doc("log_1").delete()
  );
});
