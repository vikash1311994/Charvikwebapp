const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_parent_123";
const BOB_UID = "bob_parent_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

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

test("Unauthenticated user: denied across all user-scoped collections", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  const past = new Date(Date.now() - 2000);

  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("feeds").get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("vaccines").get());
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("feeds").doc("feed_1").set({
      userId: ALICE_UID,
      feedType: "BREAST_PLUS_BOTTLE",
      breastMinutes: 12,
      bottleMl: 50,
      burpedMidFeed: true,
      uprightHoldMinutes: 25,
      salineBeforeFeed: true,
      loggedAtMillis: Date.now() - 2000,
      createdAt: past,
      updatedAt: past,
    })
  );
});

test("Profile: owner can create and update weight; cross-user and invalid bounds fail", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const past = new Date(Date.now() - 5000);

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("profile").doc("charvik").set({
      userId: ALICE_UID,
      babyName: "Charvik",
      dob: "2026-10-26",
      termStatus: "late_preterm",
      dueDateReached: true,
      lastWeightKg: 2.67,
      feedIntervalHours: 2.5,
      postFeedUprightMin: 25,
      createdAt: past,
      updatedAt: past,
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("profile").doc("charvik").update({
      lastWeightKg: 3.05,
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Cross-user read & write must fail
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("profile").doc("charvik").get());
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("profile").doc("charvik").update({
      lastWeightKg: 4.0,
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Ghost field & future timestamp fail
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("profile").doc("charvik_bad").set({
      id: "charvik_bad",
      userId: ALICE_UID,
      babyName: "Charvik",
      dob: "2026-10-26",
      termStatus: "late_preterm",
      dueDateReached: true,
      lastWeightKg: 2.67,
      feedIntervalHours: 2.5,
      postFeedUprightMin: 25,
      createdAt: past,
      updatedAt: past,
    })
  );
});

test("FeedLog & DigestionLog: Kotlin payloads, list queries, and partial updates succeed", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const past = new Date(Date.now() - 5000);

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("feeds").doc("feed_1").set({
      userId: ALICE_UID,
      feedType: "BREAST_PLUS_BOTTLE",
      breastMinutes: 12,
      bottleMl: 55,
      burpedMidFeed: true,
      uprightHoldMinutes: 25,
      salineBeforeFeed: true,
      note: "Took 55ml smoothly with horizontal nipple",
      loggedAtMillis: Date.now() - 5000,
      createdAt: past,
      updatedAt: past,
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("digestionLogs").doc("dig_1").set({
      userId: ALICE_UID,
      wetDiaper: true,
      stoolPassed: true,
      stoolColor: "YELLOW_SEEDY",
      refluxOrSpitUp: "MILD_MUCUS",
      hiccups: false,
      gasOrDiscomfort: false,
      loggedAtMillis: Date.now() - 4000,
      createdAt: past,
      updatedAt: past,
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("feeds").orderBy("loggedAtMillis", "desc").get()
  );
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("digestionLogs").orderBy("loggedAtMillis", "desc").get()
  );

  // Cross-user list denied
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("feeds").get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("digestionLogs").get());

  // Invalid enum rejected
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("digestionLogs").doc("dig_bad").set({
      userId: ALICE_UID,
      wetDiaper: true,
      stoolPassed: false,
      stoolColor: "INVALID_COLOR",
      refluxOrSpitUp: "NONE",
      hiccups: false,
      gasOrDiscomfort: false,
      loggedAtMillis: Date.now() - 4000,
      createdAt: past,
      updatedAt: past,
    })
  );
});

test("CareEvents, Vaccines, and Alarms: create, update, and security bounds", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const past = new Date(Date.now() - 5000);

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("careEvents").doc("evt_1").set({
      userId: ALICE_UID,
      category: "VITALS",
      title: "Temperature & Breathing Check",
      numericValue: 98.4,
      secondaryValue: 44,
      flagAlert: false,
      dateKey: "2026-11-18",
      loggedAtMillis: Date.now() - 4000,
      createdAt: past,
      updatedAt: past,
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("vaccines").doc("BCG").set({
      userId: ALICE_UID,
      vaccineCode: "BCG",
      name: "BCG",
      stageLabel: "At Birth",
      dueDateIso: "2026-10-26",
      status: "PENDING",
      reminderEnabled: true,
      createdAt: past,
      updatedAt: past,
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("vaccines").doc("BCG").update({
      status: "DONE",
      administeredDateIso: "2026-10-26",
      hospitalNote: "Apollo Hospitals Jubilee Hills",
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("alarms").doc("alarm_sun").set({
      userId: ALICE_UID,
      alarmType: "VITAMIN_D_SUN",
      label: "Morning Indirect Sun & Vitamin D3",
      hour: 7,
      minute: 30,
      triggerAtMillis: Date.now() + 3600000,
      repeatDaily: true,
      enabled: true,
      createdAt: past,
      updatedAt: past,
    })
  );

  // Reject immutable vaccineCode change
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("vaccines").doc("BCG").update({
      vaccineCode: "OPV_0",
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Reject out-of-range alarm hour
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("alarms").doc("alarm_bad").set({
      userId: ALICE_UID,
      alarmType: "FEED",
      label: "Bad Hour",
      hour: 25,
      minute: 0,
      triggerAtMillis: 0,
      repeatDaily: false,
      enabled: true,
      createdAt: past,
      updatedAt: past,
    })
  );
});
