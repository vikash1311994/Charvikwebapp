'use strict';

(() => {
  const STORAGE_KEY = 'charvik_pwa_state_v1';
  const THEME_KEY = 'charvik_pwa_theme_v1';
  const INSTALL_DISMISSED_KEY = 'charvik_ios_install_dismissed';

  const DOB_ISO = '2026-10-26';
  const MAX_SUCTIONS = 4;
  const MIN_WET_DIAPERS = 6;
  const MAX_RESTING_BPM = 60;
  const FEVER_F = 100.4;
  const HYPO_F = 97.7;

  const DAILY_ROUTINE_SPECS = [
    {
      key: 'VITAMIN_D3',
      title: 'Vitamin D3 Drops',
      subtitle: 'Give prescribed daily dose (typically 400 IU)',
      hint: 'Daily',
    },
    {
      key: 'MORNING_SUN',
      title: 'Indirect Morning Sunlight',
      subtitle: '10–15 min gentle indirect morning light near window',
      hint: '7:30–8:30 AM',
    },
    {
      key: 'FACE_WIPE',
      title: 'Plain Water Face Wipe',
      subtitle: 'Lukewarm plain water only — no soaps or creams for cheek milia',
      hint: 'Morning / Bath',
    },
    {
      key: 'HEAD_MASSAGE',
      title: 'Light Head Massage',
      subtitle: '2–3 min gentle stroke — ZERO pressure on soft spot (fontanelle)',
      hint: 'Calm alert',
    },
  ];

  const DIGESTION_TIPS = [
    'Mid-feed burp at 25–30 ml releases swallowed air and eases silent reflux.',
    'Keep Charvik upright at ~45° for 20–30 min after every feed before laying flat on his back.',
    'Use paced bottle feeding with a slow-flow (SS / 0m+) nipple held horizontal.',
    'Wait at least 30 min after feeding before gentle bicycle-legs or tummy time for gas.',
    'Immature diaphragm hiccups and cheek milia are normal in late-preterm newborns — no ghutti or water needed.',
    'Hydration check: aim for ≥ 6 soaked wet diapers in every 24-hour window.',
  ];

  const SAFETY_RULES = [
    {
      title: 'Safe Sleep Position (Back to Sleep)',
      detail: 'Always flat on his back on a firm, flat mattress. NO pillows, loose blankets, wedges, or sleeping on an adult’s chest.',
      restricted: false,
    },
    {
      title: 'NO Direct Sunlight',
      detail: 'Only indirect morning light (7:30–8:30 AM) for 10–15 min. Direct sunlight risks overheating and newborn UV burn.',
      restricted: true,
    },
    {
      title: 'NO Mustard Oil or Kajal',
      detail: 'Never apply mustard oil on skin or kajal in eyes — not even homemade badam kajal (risk of irritation, blocked tear ducts, or lead).',
      restricted: true,
    },
    {
      title: 'NO Medicated Nasal Drops',
      detail: 'Use ONLY plain saline drops (Noblok) 1–2 drops/nostril 10 min pre-feed; cap gentle suction at 3–4 times/day.',
      restricted: true,
    },
    {
      title: 'NO Plain Water, Honey, or Janam Ghutti',
      detail: 'Strictly breast milk / prescribed formula only until 6 months. Honey carries infant botulism risk; water dilutes electrolytes.',
      restricted: true,
    },
  ];

  const APOLLO_VACCINE_SEEDS = [
    { code: 'BCG', name: 'BCG (Tuberculosis)', stage: 'At Birth', offsetDays: 0, offsetMonths: 0, done: true, purpose: 'Protects against severe childhood tuberculosis' },
    { code: 'OPV_0', name: 'OPV 0 (Oral Polio)', stage: 'At Birth', offsetDays: 0, offsetMonths: 0, done: true, purpose: 'Birth dose oral polio drop' },
    { code: 'HEPB_1', name: 'Hepatitis B (Birth Dose)', stage: 'At Birth', offsetDays: 0, offsetMonths: 0, done: true, purpose: 'First hepatitis B protection dose' },

    { code: 'DTWP_1', name: 'DTwP / DTaP 1 (Hexavalent)', stage: '6 Weeks', offsetDays: 42, offsetMonths: 0, done: false, purpose: 'Diphtheria, Tetanus, Pertussis dose 1' },
    { code: 'IPV_1', name: 'IPV 1 (Inactivated Polio)', stage: '6 Weeks', offsetDays: 42, offsetMonths: 0, done: false, purpose: 'Injectable polio dose 1' },
    { code: 'HIB_1', name: 'Hib 1', stage: '6 Weeks', offsetDays: 42, offsetMonths: 0, done: false, purpose: 'Haemophilus influenzae type b dose 1' },
    { code: 'HEPB_2', name: 'Hepatitis B Dose 2', stage: '6 Weeks', offsetDays: 42, offsetMonths: 0, done: false, purpose: 'Second hepatitis B dose (often in 6-in-1)' },
    { code: 'ROTA_1', name: 'Rotavirus 1 (Oral)', stage: '6 Weeks', offsetDays: 42, offsetMonths: 0, done: false, purpose: 'Protects against severe rotavirus diarrhea' },
    { code: 'PCV_1', name: 'PCV 1 (Pneumococcal)', stage: '6 Weeks', offsetDays: 42, offsetMonths: 0, done: false, purpose: 'Pneumococcal conjugate vaccine dose 1' },

    { code: 'DTWP_2', name: 'DTwP / DTaP 2', stage: '10 Weeks', offsetDays: 70, offsetMonths: 0, done: false, purpose: 'Diphtheria, Tetanus, Pertussis dose 2' },
    { code: 'IPV_2', name: 'IPV 2', stage: '10 Weeks', offsetDays: 70, offsetMonths: 0, done: false, purpose: 'Injectable polio dose 2' },
    { code: 'HIB_2', name: 'Hib 2', stage: '10 Weeks', offsetDays: 70, offsetMonths: 0, done: false, purpose: 'Haemophilus influenzae type b dose 2' },
    { code: 'ROTA_2', name: 'Rotavirus 2 (Oral)', stage: '10 Weeks', offsetDays: 70, offsetMonths: 0, done: false, purpose: 'Rotavirus oral vaccine dose 2' },
    { code: 'PCV_2', name: 'PCV 2', stage: '10 Weeks', offsetDays: 70, offsetMonths: 0, done: false, purpose: 'Pneumococcal conjugate vaccine dose 2' },

    { code: 'DTWP_3', name: 'DTwP / DTaP 3', stage: '14 Weeks', offsetDays: 98, offsetMonths: 0, done: false, purpose: 'Diphtheria, Tetanus, Pertussis dose 3' },
    { code: 'IPV_3', name: 'IPV 3', stage: '14 Weeks', offsetDays: 98, offsetMonths: 0, done: false, purpose: 'Injectable polio dose 3' },
    { code: 'HIB_3', name: 'Hib 3', stage: '14 Weeks', offsetDays: 98, offsetMonths: 0, done: false, purpose: 'Haemophilus influenzae type b dose 3' },
    { code: 'ROTA_3', name: 'Rotavirus 3 (Oral)', stage: '14 Weeks', offsetDays: 98, offsetMonths: 0, done: false, purpose: 'Rotavirus oral vaccine dose 3' },
    { code: 'PCV_3', name: 'PCV 3', stage: '14 Weeks', offsetDays: 98, offsetMonths: 0, done: false, purpose: 'Pneumococcal conjugate vaccine dose 3' },

    { code: 'FLU_1', name: 'Influenza (IIV) Dose 1', stage: '6 Months', offsetDays: 0, offsetMonths: 6, done: false, purpose: 'Seasonal flu protection (priming dose 1)' },
    { code: 'FLU_2', name: 'Influenza (IIV) Dose 2', stage: '7 Months', offsetDays: 0, offsetMonths: 7, done: false, purpose: 'Seasonal flu protection (priming dose 2)' },
    { code: 'MMR_1', name: 'MMR 1', stage: '9 Months', offsetDays: 0, offsetMonths: 9, done: false, purpose: 'Measles, Mumps, Rubella dose 1' },
    { code: 'TCV_1', name: 'Typhoid Conjugate (TCV)', stage: '9 Months', offsetDays: 0, offsetMonths: 9, done: false, purpose: 'Single-dose typhoid conjugate vaccine' },
    { code: 'HEPA_1', name: 'Hepatitis A Dose 1', stage: '12 Months', offsetDays: 0, offsetMonths: 12, done: false, purpose: 'Hepatitis A primary protection' },
    { code: 'MMR_2', name: 'MMR 2', stage: '15 Months', offsetDays: 0, offsetMonths: 15, done: false, purpose: 'Measles, Mumps, Rubella dose 2' },
    { code: 'VARICELLA_1', name: 'Varicella 1 (Chickenpox)', stage: '15 Months', offsetDays: 0, offsetMonths: 15, done: false, purpose: 'Chickenpox protection dose 1' },
    { code: 'PCV_B', name: 'PCV Booster', stage: '15 Months', offsetDays: 0, offsetMonths: 15, done: false, purpose: 'Pneumococcal booster dose' },
    { code: 'DTP_B1', name: 'DTwP / DTaP Booster 1 + IPV B1 + Hib B1', stage: '16–18 Months', offsetDays: 0, offsetMonths: 17, done: false, purpose: 'First childhood booster series' },
    { code: 'HEPA_2', name: 'Hepatitis A Dose 2', stage: '18 Months', offsetDays: 0, offsetMonths: 18, done: false, purpose: 'Long-term Hepatitis A immunity' },
  ];

  // ---------- Date & Clinical Math Helpers ----------
  function parseIsoDate(iso) {
    const [y, m, d] = iso.split('-').map(Number);
    return new Date(y, m - 1, d, 0, 0, 0, 0);
  }

  function toIsoDate(date) {
    const y = date.getFullYear();
    const m = String(date.getMonth() + 1).padStart(2, '0');
    const d = String(date.getDate()).padStart(2, '0');
    return `${y}-${m}-${d}`;
  }

  function todayIso() {
    return toIsoDate(new Date());
  }

  function computeDueIso(dobIso, offsetDays, offsetMonths) {
    const dt = parseIsoDate(dobIso);
    if (offsetMonths) dt.setMonth(dt.getMonth() + offsetMonths);
    if (offsetDays) dt.setDate(dt.getDate() + offsetDays);
    return toIsoDate(dt);
  }

  function daysUntilIso(targetIso) {
    const today = parseIsoDate(todayIso());
    const target = parseIsoDate(targetIso);
    return Math.round((target.getTime() - today.getTime()) / 86400000);
  }

  function formatDisplayDate(iso) {
    const dt = parseIsoDate(iso);
    return dt.toLocaleDateString('en-US', { day: '2-digit', month: 'short', year: 'numeric' });
  }

  function formatTime(ms) {
    return new Date(ms).toLocaleTimeString('en-US', { hour: 'numeric', minute: '2-digit', hour12: true });
  }

  function formatClock(hour, minute) {
    const dt = new Date();
    dt.setHours(hour, minute, 0, 0);
    return dt.toLocaleTimeString('en-US', { hour: 'numeric', minute: '2-digit', hour12: true });
  }

  function formatMmSs(totalSec) {
    const safe = Math.max(0, Math.floor(totalSec));
    const m = String(Math.floor(safe / 60)).padStart(2, '0');
    const s = String(safe % 60).padStart(2, '0');
    return `${m}:${s}`;
  }

  function calculateAge(dobIso) {
    const dob = parseIsoDate(dobIso);
    const today = parseIsoDate(todayIso());
    const diffDays = Math.max(0, Math.floor((today.getTime() - dob.getTime()) / 86400000));
    return {
      daysOld: diffDays,
      weeks: Math.floor(diffDays / 7),
      remDays: diffDays % 7,
      dobDisplay: formatDisplayDate(dobIso),
    };
  }

  function computeFeedTargets(weightKg) {
    const w = Math.max(1.5, Math.min(15, Number(weightKg) || 2.67));
    const dailyMin = Math.round(w * 150);
    const dailyMax = Math.round(w * 180);
    const unlocked = w >= 3.0;
    const perFeedMin = unlocked ? 60 : 45;
    const perFeedMax = unlocked ? 70 : 60;
    const progressTo3Kg = Math.max(0, Math.min(1, (w - 2.2) / (3.0 - 2.2)));
    return { weightKg: w, dailyMin, dailyMax, perFeedMin, perFeedMax, unlocked, progressTo3Kg };
  }

  function escHtml(str) {
    return String(str ?? '')
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;');
  }

  // ---------- Default Seed State ----------
  function createSeedState() {
    const vaccines = APOLLO_VACCINE_SEEDS.map((v) => ({
      code: v.code,
      name: v.name,
      stage: v.stage,
      dueDateIso: computeDueIso(DOB_ISO, v.offsetDays, v.offsetMonths),
      done: v.done,
      administeredDateIso: v.done ? DOB_ISO : '',
      hospitalNote: v.done ? 'Apollo Hospitals (Birth Dose)' : '',
      purpose: v.purpose,
    }));

    return {
      version: 1,
      profile: {
        name: 'Charvik',
        dobIso: DOB_ISO,
        weightKg: 2.67,
        correctedNote: 'Late-preterm • Reached ~40 weeks corrected age Nov 15–16',
        conditions: [
          'Mild silent reflux (upper-throat/nasal milk mucus)',
          'Newborn cheek milia',
          'Immature diaphragm hiccups',
        ],
      },
      feeds: [],
      digestionLogs: [],
      suctions: [], // { id, dateKey, ts }
      routineChecks: {}, // { 'YYYY-MM-DD': ['VITAMIN_D3', ...] }
      pumpSessions: [], // { id, minutes, ml, ts }
      vitalsLogs: [], // { id, tempF, bpm, redFlag, ts }
      weightHistory: [{ id: 'seed_w', weightKg: 2.67, note: 'Starting baseline weight', ts: Date.now() }],
      vaccines,
      alarms: [
        {
          id: 'morning_sun_d3',
          label: 'Indirect Morning Sun (10–15m) & Vitamin D3 Drops',
          hour: 7,
          minute: 30,
          repeatDaily: true,
          enabled: true,
          category: 'ROUTINE',
          lastFiredDate: '',
        },
      ],
      timers: {
        reflux: { running: false, durationSec: 25 * 60, endAtMs: 0, selectedMin: 25, completedAlert: false },
        pump: { running: false, durationSec: 12 * 60, startAtMs: 0, selectedMin: 12, phaseChimePlayed: false, completedAlert: false, pumpedMl: 50 },
        snoozeUntilMs: 0,
        snoozeTitle: '',
      },
    };
  }

  function loadState() {
    try {
      const raw = localStorage.getItem(STORAGE_KEY);
      if (!raw) {
        const seed = createSeedState();
        localStorage.setItem(STORAGE_KEY, JSON.stringify(seed));
        return seed;
      }
      const parsed = JSON.parse(raw);
      const seed = createSeedState();
      return {
        ...seed,
        ...parsed,
        profile: { ...seed.profile, ...(parsed.profile || {}) },
        timers: {
          ...seed.timers,
          ...(parsed.timers || {}),
          reflux: { ...seed.timers.reflux, ...(parsed.timers?.reflux || {}) },
          pump: { ...seed.timers.pump, ...(parsed.timers?.pump || {}) },
        },
      };
    } catch {
      return createSeedState();
    }
  }

  let state = loadState();
  function saveState() {
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(state));
    } catch {
      // Ignore quota errors gracefully
    }
  }

  // ---------- Ephemeral UI State ----------
  let activeTab = 'today';
  let vaccineFilter = 'pending'; // pending | done | all
  let tempF = 98.4;
  let breathing = {
    running: false,
    startedAtMs: 0,
    remainingSec: 60,
    taps: 0,
    finished: false,
    retractions: false,
    flaring: false,
    grunting: false,
  };

  // ---------- Web Audio API Alarm Engine (iOS Safari Safe) ----------
  let audioCtx = null;
  let audioUnlocked = false;
  let alarmIntervalId = null;
  let isRinging = false;
  let ringingTitle = '';

  function ensureAudioContext() {
    const AudioCtx = window.AudioContext || window.webkitAudioContext;
    if (!AudioCtx) return null;
    if (!audioCtx) audioCtx = new AudioCtx();
    if (audioCtx.state === 'suspended') {
      audioCtx.resume().catch(() => {});
    }
    return audioCtx;
  }

  function updateAudioButtonUI() {
    const btn = document.getElementById('btn-audio-unlock');
    const icon = document.getElementById('audio-status-icon');
    const label = document.getElementById('audio-status-label');
    if (!btn) return;
    if (audioUnlocked) {
      btn.classList.add('audio-on');
      icon.textContent = '🔊';
      label.textContent = 'Ready';
    } else {
      btn.classList.remove('audio-on');
      icon.textContent = '🔇';
      label.textContent = 'Unlock';
    }
  }

  function unlockAudio(withChime = true) {
    const ctx = ensureAudioContext();
    if (!ctx) {
      showToast('Web Audio not supported in this browser');
      return;
    }
    ctx.resume().then(() => {
      audioUnlocked = true;
      updateAudioButtonUI();
      if (withChime) {
        playToneSequence([
          { freq: 587.33, dur: 0.12, delay: 0, gain: 0.16 },
          { freq: 880, dur: 0.18, delay: 0.13, gain: 0.18 },
        ]);
        showToast('iPhone / Browser alarm sound unlocked');
      }
    }).catch(() => {
      showToast('Tap Sound button again to enable alarm audio');
    });
  }

  function playToneSequence(notes) {
    const ctx = ensureAudioContext();
    if (!ctx) return;
    const now = ctx.currentTime;
    notes.forEach(({ freq, dur, delay, gain = 0.28, type = 'triangle' }) => {
      const osc = ctx.createOscillator();
      const g = ctx.createGain();
      osc.type = type;
      osc.frequency.setValueAtTime(freq, now + delay);
      g.gain.setValueAtTime(0.0001, now + delay);
      g.gain.exponentialRampToValueAtTime(gain, now + delay + 0.02);
      g.gain.exponentialRampToValueAtTime(0.0001, now + delay + dur);
      osc.connect(g);
      g.connect(ctx.destination);
      osc.start(now + delay);
      osc.stop(now + delay + dur + 0.03);
    });
  }

  function playNurseryAlarmBurst() {
    playToneSequence([
      { freq: 659.25, dur: 0.22, delay: 0.0, gain: 0.34, type: 'triangle' },
      { freq: 880.0, dur: 0.22, delay: 0.24, gain: 0.36, type: 'triangle' },
      { freq: 1046.5, dur: 0.36, delay: 0.48, gain: 0.38, type: 'sine' },
      { freq: 880.0, dur: 0.22, delay: 0.90, gain: 0.34, type: 'triangle' },
      { freq: 1046.5, dur: 0.42, delay: 1.14, gain: 0.38, type: 'sine' },
    ]);
    if (navigator.vibrate) {
      navigator.vibrate([260, 140, 260, 140, 400]);
    }
  }

  function playPhaseChime() {
    playToneSequence([
      { freq: 523.25, dur: 0.2, delay: 0, gain: 0.28 },
      { freq: 659.25, dur: 0.2, delay: 0.22, gain: 0.28 },
      { freq: 783.99, dur: 0.35, delay: 0.44, gain: 0.3 },
    ]);
    if (navigator.vibrate) navigator.vibrate([200, 100, 200]);
  }

  function startLoopingAlarm(title) {
    stopLoopingAlarm();
    isRinging = true;
    ringingTitle = title || 'Charvik Care Alarm';
    audioUnlocked = true;
    updateAudioButtonUI();
    playNurseryAlarmBurst();
    alarmIntervalId = setInterval(playNurseryAlarmBurst, 2300);
    renderRingingBanner();
  }

  function stopLoopingAlarm() {
    if (alarmIntervalId) {
      clearInterval(alarmIntervalId);
      alarmIntervalId = null;
    }
    isRinging = false;
    ringingTitle = '';
    if (navigator.vibrate) navigator.vibrate(0);
    renderRingingBanner();
  }

  function snoozeLoopingAlarm() {
    const title = ringingTitle || 'Care Reminder';
    stopLoopingAlarm();
    state.timers.snoozeUntilMs = Date.now() + 5 * 60 * 1000;
    state.timers.snoozeTitle = `${title} (Snoozed)`;
    saveState();
    showToast('Snoozed for 5 minutes');
  }

  function renderRingingBanner() {
    const el = document.getElementById('ringing-banner');
    const titleEl = document.getElementById('ringing-title');
    if (!el) return;
    if (isRinging) {
      titleEl.textContent = ringingTitle;
      el.classList.remove('hidden');
    } else {
      el.classList.add('hidden');
    }
  }

  // ---------- Toast ----------
  let toastTimer = null;
  function showToast(msg) {
    const el = document.getElementById('toast');
    if (!el) return;
    el.textContent = msg;
    el.classList.remove('hidden');
    clearTimeout(toastTimer);
    toastTimer = setTimeout(() => el.classList.add('hidden'), 3000);
  }

  // ---------- Theme (Light + Night-Feed Dark Mode) ----------
  function applyTheme(mode) {
    document.documentElement.setAttribute('data-theme', mode);
    const meta = document.getElementById('theme-color-meta');
    const icon = document.getElementById('theme-icon');
    if (meta) meta.setAttribute('content', mode === 'dark' ? '#101918' : '#FAF6F0');
    if (icon) icon.textContent = mode === 'dark' ? '☀️' : '🌙';
  }

  function initTheme() {
    const saved = localStorage.getItem(THEME_KEY);
    if (saved === 'dark' || saved === 'light') {
      applyTheme(saved);
    } else if (window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches) {
      applyTheme('dark');
    } else {
      applyTheme('light');
    }
  }

  function toggleTheme() {
    const current = document.documentElement.getAttribute('data-theme') === 'dark' ? 'dark' : 'light';
    const next = current === 'dark' ? 'light' : 'dark';
    localStorage.setItem(THEME_KEY, next);
    applyTheme(next);
    showToast(next === 'dark' ? 'Night-Feed Dark Mode on' : 'Warm Cream Light Mode on');
  }

  // ---------- Computed Metrics ----------
  function getTodayMetrics() {
    const now = Date.now();
    const todayKey = todayIso();
    const cutoff24h = now - 24 * 60 * 60 * 1000;

    const todayFeeds = state.feeds.filter((f) => toIsoDate(new Date(f.ts)) === todayKey);
    const todayBottleMl = todayFeeds.reduce((sum, f) => sum + (Number(f.bottleMl) || 0), 0);
    const lastFeedTs = state.feeds.length ? Math.max(...state.feeds.map((f) => f.ts)) : null;

    const logs24h = state.digestionLogs.filter((d) => d.ts >= cutoff24h);
    const wet24h = logs24h.filter((d) => d.wetDiaper).length;
    const stools24h = logs24h.filter((d) => d.stoolPassed).length;

    const suctionsToday = state.suctions.filter((s) => s.dateKey === todayKey).length;
    const completedRoutines = new Set(state.routineChecks[todayKey] || []);

    return {
      now,
      todayKey,
      todayFeeds,
      todayBottleMl,
      lastFeedTs,
      wet24h,
      stools24h,
      suctionsToday,
      completedRoutines,
    };
  }

  // ---------- Ring SVG Helper ----------
  function buildRingCard({ title, valueText, subText, progress, colorVar, isAlert }) {
    const r = 27;
    const c = 2 * Math.PI * r;
    const clamped = Math.max(0, Math.min(1, progress));
    const dashOffset = c * (1 - clamped);
    return `
      <div class="ring-card ${isAlert ? 'alert' : ''}">
        <div class="ring-wrap">
          <svg viewBox="0 0 68 68">
            <circle cx="34" cy="34" r="${r}" fill="none" stroke="var(--surface-variant)" stroke-width="7"></circle>
            <circle cx="34" cy="34" r="${r}" fill="none" stroke="var(${colorVar})" stroke-width="7"
              stroke-linecap="round" stroke-dasharray="${c.toFixed(1)}" stroke-dashoffset="${dashOffset.toFixed(1)}"></circle>
          </svg>
          <div class="ring-value mono">${escHtml(valueText)}</div>
        </div>
        <div class="ring-title">${escHtml(title)}</div>
        <div class="ring-sub">${escHtml(subText)}</div>
      </div>
    `;
  }

  // ---------- Screen 1: Today Dashboard ----------
  function renderTodayScreen() {
    const m = getTodayMetrics();
    const age = calculateAge(state.profile.dobIso);
    const targets = computeFeedTargets(state.profile.weightKg);

    let feedBannerClass = 'card-primary';
    let feedStatusText = 'No feeds logged yet';
    let feedBadge = `${targets.perFeedMin}–${targets.perFeedMax} ml`;
    if (m.lastFeedTs) {
      const minsAgo = Math.max(0, Math.floor((m.now - m.lastFeedTs) / 60000));
      const hrs = Math.floor(minsAgo / 60);
      const remM = minsAgo % 60;
      if (minsAgo >= 180) {
        feedBannerClass = 'card-error';
        feedStatusText = `⚠ Feed gap > 3 hrs (${hrs}h ${remM}m ago)`;
      } else if (minsAgo >= 120) {
        feedBannerClass = 'card-secondary';
        feedStatusText = `Feed window open (${hrs}h ${remM}m ago)`;
      } else {
        feedStatusText = `Last feed ${hrs}h ${remM}m ago`;
      }
    }

    const reflux = state.timers.reflux;
    const refluxRemSec = reflux.running ? Math.max(0, Math.ceil((reflux.endAtMs - Date.now()) / 1000)) : 0;

    const recentFeedsHtml = state.feeds.slice(0, 5).map((f) => {
      const parts = [];
      if (f.breastMin > 0) parts.push(`${f.breastMin}m breast`);
      if (f.bottleMl > 0) parts.push(`${f.bottleMl} ml bottle`);
      if (f.burpedMidFeed) parts.push('Mid-feed burp');
      if (f.salinePreFeed) parts.push('Noblok saline');
      return `
        <div class="list-row list-row-clean">
          <div style="flex:1;">
            <div style="font-weight:700;font-size:14px;">${escHtml(f.feedTypeLabel)}</div>
            <div class="small muted">${escHtml(parts.join(' • ') || 'Feed logged')}</div>
            ${f.note ? `<div class="xs" style="color:var(--primary);margin-top:2px;">${escHtml(f.note)}</div>` : ''}
          </div>
          <div class="mono xs muted">${formatTime(f.ts)}</div>
          <button type="button" class="del-btn" data-del-feed="${f.id}" aria-label="Delete feed">×</button>
        </div>
      `;
    }).join('');

    const recentDigHtml = state.digestionLogs.slice(0, 4).map((d) => {
      const headline = [
        d.wetDiaper ? 'Wet Diaper' : '',
        d.stoolPassed ? `Stool (${d.stoolColor})` : '',
      ].filter(Boolean).join(' + ') || 'Digestion Check';
      const sub = [
        d.reflux && d.reflux !== 'None' ? `Reflux: ${d.reflux}` : '',
        d.hiccups ? 'Hiccups' : '',
        d.gas ? 'Gas / straining' : '',
      ].filter(Boolean).join(' • ') || 'Logged cleanly';
      return `
        <div class="list-row list-row-clean">
          <div style="flex:1;">
            <div style="font-weight:700;font-size:14px;">${escHtml(headline)}</div>
            <div class="small muted">${escHtml(sub)}</div>
          </div>
          <div class="mono xs muted">${formatTime(d.ts)}</div>
          <button type="button" class="del-btn" data-del-dig="${d.id}" aria-label="Delete digestion log">×</button>
        </div>
      `;
    }).join('');

    return `
      <!-- Hero Baby Banner -->
      <section class="card hero-card">
        <div class="hero-bg"></div>
        <div class="hero-content">
          <div class="hero-row">
            <div>
              <span class="pill">DOB ${escHtml(age.dobDisplay)}</span>
              <h1 class="serif-title" style="font-size:26px;margin-top:6px;">Charvik • ${age.daysOld} days</h1>
              <div class="small muted">${age.weeks}w ${age.remDays}d • ${escHtml(state.profile.correctedNote)}</div>
            </div>
            <button type="button" class="weight-badge" id="btn-open-weight">
              <span class="mono">${targets.weightKg.toFixed(2)} kg</span>
              <span class="sub">Edit weight ✎</span>
            </button>
          </div>

          <div class="conditions-row">
            ${state.profile.conditions.map((c) => `<span class="condition-chip">${escHtml(c)}</span>`).join('')}
          </div>

          <div style="margin-top:12px;">
            <div style="display:flex;justify-content:space-between;align-items:center;gap:8px;">
              <span class="xs" style="font-weight:700;color:var(--primary);">
                ${targets.unlocked
                  ? '✓ 3.0 kg milestone reached • 60–70 ml/feed unlocked!'
                  : `Progress toward 3.0 kg unlock (expected gain 20–30 g/day)`}
              </span>
              <span class="mono xs muted">${Math.round(targets.progressTo3Kg * 100)}%</span>
            </div>
            <div class="progress-track">
              <div class="progress-fill" style="width:${(targets.progressTo3Kg * 100).toFixed(1)}%"></div>
            </div>
          </div>
        </div>
      </section>

      <!-- Live Feed Status & Formula Card -->
      <section class="card ${feedBannerClass}">
        <div style="display:flex;justify-content:space-between;align-items:center;gap:8px;margin-bottom:6px;">
          <div style="font-weight:800;font-size:15px;">${escHtml(feedStatusText)}</div>
          <span class="pill mono">${escHtml(feedBadge)}</span>
        </div>
        <div class="small" style="opacity:0.92;">
          Daily target: <strong class="mono">${targets.dailyMin}–${targets.dailyMax} ml/day</strong> (150–180 ml × ${targets.weightKg.toFixed(2)} kg) • Feed every 2.0–2.5 hrs (8–10/day).
        </div>
        <div class="xs" style="margin-top:4px;opacity:0.86;">
          Breast 10–15 min first → Paced horizontal bottle (SS/0m+ nipple) • Burp at 25–30 ml.
        </div>

        ${(reflux.running || reflux.completedAlert) ? `
          <div class="list-row" style="background:var(--surface);color:var(--ink);margin-top:10px;">
            <div>
              <div style="font-weight:800;font-size:13px;color:var(--primary);">
                ${reflux.completedAlert ? 'Reflux Hold Complete!' : '45° Upright Reflux Hold'}
              </div>
              <div class="mono small">
                ${reflux.completedAlert ? 'Safe to lay Charvik flat on his back' : `${formatMmSs(refluxRemSec)} remaining`}
              </div>
            </div>
            <button type="button" class="btn btn-sm btn-secondary" id="btn-stop-reflux-today">
              ${reflux.completedAlert ? 'Done' : 'Stop'}
            </button>
          </div>
        ` : ''}
      </section>

      <!-- One-Handed Quick Action Buttons -->
      <div class="btn-row">
        <button type="button" class="btn btn-primary" id="btn-open-feed">🍼 Log Feed</button>
        <button type="button" class="btn btn-tonal" id="btn-open-digestion">🧷 Diaper / Gas</button>
      </div>
      <div class="btn-row" style="margin-bottom:14px;">
        <button type="button" class="btn btn-amber" id="btn-quick-reflux">
          ⏱️ ${reflux.running ? `Reflux ${formatMmSs(refluxRemSec)}` : '25m Reflux Hold'}
        </button>
        <button type="button" class="btn btn-outline" id="btn-quick-weight">⚖️ Log Weight</button>
      </div>

      <!-- 3 Daily Progress Rings -->
      <div class="rings-grid">
        ${buildRingCard({
          title: 'Bottle Intake',
          valueText: `${m.todayBottleMl}`,
          subText: `${targets.dailyMin}–${targets.dailyMax} ml`,
          progress: m.todayBottleMl / Math.max(1, targets.dailyMin),
          colorVar: '--primary',
          isAlert: false,
        })}
        ${buildRingCard({
          title: 'Wet Diapers',
          valueText: `${m.wet24h}/6`,
          subText: '≥ 6 in 24h',
          progress: m.wet24h / MIN_WET_DIAPERS,
          colorVar: '--tertiary',
          isAlert: state.digestionLogs.length > 0 && m.wet24h < MIN_WET_DIAPERS,
        })}
        ${buildRingCard({
          title: 'Suctions',
          valueText: `${m.suctionsToday}/4`,
          subText: 'Max 3–4/day',
          progress: m.suctionsToday / MAX_SUCTIONS,
          colorVar: '--secondary',
          isAlert: m.suctionsToday >= MAX_SUCTIONS,
        })}
      </div>

      <!-- Daily Care Checklist -->
      <div class="section-title serif-title">
        <span>Daily Care Checklist</span>
        <span class="xs muted mono">${m.completedRoutines.size}/${DAILY_ROUTINE_SPECS.length}</span>
      </div>
      <section class="card" style="padding:6px 14px;">
        ${DAILY_ROUTINE_SPECS.map((item) => {
          const done = m.completedRoutines.has(item.key);
          return `
            <button type="button" class="checklist-item ${done ? 'done' : ''}" data-routine="${item.key}">
              <span class="check-circle">${done ? '✓' : ''}</span>
              <span style="flex:1;">
                <span class="check-title" style="display:block;font-weight:700;font-size:14px;">${escHtml(item.title)}</span>
                <span class="xs muted" style="display:block;">${escHtml(item.subtitle)}</span>
              </span>
              <span class="pill" style="background:var(--surface-variant);color:var(--muted);">${escHtml(item.hint)}</span>
            </button>
          `;
        }).join('')}
      </section>

      <!-- Recent Activity -->
      <div class="section-title serif-title">
        <span>Recent Feeds & Output</span>
        <span class="xs muted">${m.todayFeeds.length} feeds today</span>
      </div>
      ${(state.feeds.length === 0 && state.digestionLogs.length === 0)
        ? `<section class="card card-muted" style="text-align:center;">
             <div style="font-weight:700;margin-bottom:4px;">No logs yet today</div>
             <div class="small muted">Tap <strong>Log Feed</strong> or <strong>Diaper / Gas</strong> above to start tracking Charvik’s 24-hour intake and hydration.</div>
           </section>`
        : `<div>${recentFeedsHtml}${recentDigHtml}</div>`
      }
    `;
  }

  // ---------- Screen 2: Care Timers ----------
  function renderTimersScreen() {
    const m = getTodayMetrics();
    const targets = computeFeedTargets(state.profile.weightKg);
    const reflux = state.timers.reflux;
    const pump = state.timers.pump;

    const refluxRem = reflux.running
      ? Math.max(0, Math.ceil((reflux.endAtMs - Date.now()) / 1000))
      : (reflux.completedAlert ? 0 : reflux.selectedMin * 60);
    const refluxPct = reflux.running
      ? Math.min(100, ((reflux.durationSec - refluxRem) / Math.max(1, reflux.durationSec)) * 100)
      : (reflux.completedAlert ? 100 : 0);

    const pumpElapsed = pump.running
      ? Math.min(pump.durationSec, Math.floor((Date.now() - pump.startAtMs) / 1000))
      : (pump.completedAlert ? pump.durationSec : 0);
    const pumpInStim = pump.running && pumpElapsed < 120;
    const pumpPct = pump.running
      ? Math.min(100, (pumpElapsed / Math.max(1, pump.durationSec)) * 100)
      : (pump.completedAlert ? 100 : 0);

    const suctionCapped = m.suctionsToday >= MAX_SUCTIONS;

    return `
      <h1 class="serif-title" style="font-size:24px;">Care Timers & Nasal Counter</h1>
      <p class="small muted" style="margin-bottom:14px;">Web Audio alarm chimes with Snooze/Stop • Tap <strong>Unlock Sound</strong> on iPhone before starting a timer.</p>

      <!-- iOS Audio Unlock Banner -->
      ${!audioUnlocked ? `
        <section class="card card-secondary">
          <div style="font-weight:800;margin-bottom:6px;">🔊 Enable iPhone Safari Alarm Sound</div>
          <p class="small" style="margin-bottom:10px;">iOS Safari mutes timer audio until you tap once. Tap below to unlock Web Audio chimes for Reflux, Pumping & Breathing timers.</p>
          <button type="button" class="btn btn-primary" id="btn-timers-unlock-audio">Unlock Alarm Audio Now</button>
        </section>
      ` : ''}

      <!-- 1. Reflux Hold Timer -->
      <section class="card">
        <div style="display:flex;justify-content:space-between;align-items:center;">
          <h2 style="font-size:17px;">Reflux Upright Hold (~45°)</h2>
          <span class="pill">20–30 min</span>
        </div>
        <div class="timer-big mono" style="${reflux.completedAlert ? 'color:var(--error);' : ''}">${formatMmSs(refluxRem)}</div>

        ${!reflux.running ? `
          <div class="chip-row">
            ${[20, 25, 30].map((min) => `
              <button type="button" class="chip ${reflux.selectedMin === min ? 'active' : ''}" data-reflux-min="${min}">${min} min</button>
            `).join('')}
          </div>
        ` : `
          <div class="progress-track" style="margin-bottom:10px;">
            <div class="progress-fill" style="width:${refluxPct.toFixed(1)}%"></div>
          </div>
        `}

        <p class="small muted" style="margin-bottom:12px;">Hold Charvik upright at ~45° after every feed before laying him flat on his back. Loud chime rings at 00:00.</p>
        ${!reflux.running
          ? `<button type="button" class="btn btn-primary" id="btn-start-reflux">▶ Start ${reflux.selectedMin}m Upright Hold</button>`
          : `<button type="button" class="btn btn-secondary" id="btn-stop-reflux">■ Stop Hold Timer</button>`
        }
      </section>

      <!-- 2. Two-Phase Pumping Timer -->
      <section class="card">
        <div style="display:flex;justify-content:space-between;align-items:center;gap:8px;">
          <h2 style="font-size:17px;">Two-Phase Pumping Timer</h2>
          <span class="pill ${pumpInStim ? 'pill-amber' : 'pill-terracotta'}">
            ${pump.completedAlert ? 'Complete' : pumpInStim ? 'Phase 1: Stimulation (0–2m)' : pump.running ? 'Phase 2: Pain-Free Expression' : 'Every 2.5–3 hrs'}
          </span>
        </div>

        <div class="timer-big mono" style="font-size:32px;">
          ${formatMmSs(pumpElapsed)} <span class="muted" style="font-size:20px;">/ ${formatMmSs(pump.running ? pump.durationSec : pump.selectedMin * 60)}</span>
        </div>

        ${!pump.running ? `
          <div class="chip-row">
            ${[10, 12, 15].map((min) => `
              <button type="button" class="chip ${pump.selectedMin === min ? 'active' : ''}" data-pump-min="${min}">${min} min</button>
            `).join('')}
          </div>
        ` : `
          <div class="progress-track" style="margin-bottom:10px;">
            <div class="progress-fill" style="width:${pumpPct.toFixed(1)}%;background:var(--secondary);"></div>
          </div>
        `}

        <p class="small muted" style="margin-bottom:10px;">Chimes automatically at <strong>02:00</strong> to switch from Stimulation → Pain-free Expression mode.</p>

        <div style="margin-bottom:12px;">
          ${!pump.running
            ? `<button type="button" class="btn btn-primary" id="btn-start-pump">▶ Start ${pump.selectedMin}m Pump Session</button>`
            : `<button type="button" class="btn btn-outline" id="btn-stop-pump">■ Stop Pump Timer</button>`
          }
        </div>

        <div class="field" style="margin-top:12px;">
          <label for="pump-ml-slider">Expressed Milk Logged: <span class="mono" id="pump-ml-val">${pump.pumpedMl} ml</span></label>
          <input type="range" id="pump-ml-slider" class="range-slider" min="10" max="180" step="5" value="${pump.pumpedMl}" />
        </div>
        <button type="button" class="btn btn-tonal" id="btn-save-pump">💾 Log Pump Session (${pump.pumpedMl} ml)</button>

        ${state.pumpSessions.length ? `
          <div class="xs muted" style="margin-top:10px;">
            Last pump: ${state.pumpSessions[0].minutes}m • ${state.pumpSessions[0].ml} ml at ${formatTime(state.pumpSessions[0].ts)}
          </div>
        ` : ''}
      </section>

      <!-- 3. Nasal Care Counter -->
      <section class="card ${suctionCapped ? 'card-error' : ''}">
        <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:6px;">
          <h2 style="font-size:17px;">Nasal Care (Noblok Saline)</h2>
          <span class="pill mono ${suctionCapped ? 'pill-error' : ''}">${m.suctionsToday} / 4 today</span>
        </div>
        <p class="small" style="margin-bottom:10px;">
          • Put <strong>1–2 drops Noblok plain saline</strong> per nostril 10 min before feeding to loosen milk mucus.<br/>
          • Cap gentle nasal suction at <strong>max 3–4 times/day</strong> to avoid nasal lining swelling.
        </p>
        ${suctionCapped ? `
          <p class="small" style="font-weight:800;margin-bottom:10px;">⚠ Daily cap (4/day) reached! Prefer saline drops + upright hold unless airway is blocked.</p>
        ` : ''}
        <button type="button" class="btn ${suctionCapped ? 'btn-danger' : 'btn-primary'}" id="btn-log-suction">+ Log Saline / Nasal Suction</button>
      </section>

      <!-- Feed Formula Card -->
      <section class="card card-muted">
        <h3 style="font-size:15px;margin-bottom:6px;">Charvik’s Dynamic Feed Formula</h3>
        <div class="small" style="color:var(--primary);font-weight:700;margin-bottom:4px;">
          ${targets.weightKg.toFixed(2)} kg → ${targets.dailyMin}–${targets.dailyMax} ml/day (150–180 ml × kg)
        </div>
        <div class="small muted">
          • Per feed: ${targets.perFeedMin}–${targets.perFeedMax} ml every 2.0–2.5 hrs (8–10 feeds/day).<br/>
          • Breast first (10–15 min) → Paced bottle with slow-flow (SS / 0m+) nipple held horizontal.<br/>
          • Pause at 25–30 ml for a gentle mid-feed burp.
        </div>
      </section>
    `;
  }

  // ---------- Screen 3: Digestion, Vitals & Red-Flag Safety ----------
  function renderSafetyScreen() {
    const m = getTodayMetrics();
    const tempC = ((tempF - 32) * 5) / 9;
    const tempRedFlag = tempF > FEVER_F || tempF < HYPO_F;
    const elapsedSec = breathing.running ? Math.max(1, 60 - breathing.remainingSec) : 60;
    const estBpm = breathing.finished ? breathing.taps : Math.round((breathing.taps * 60) / elapsedSec);
    const breathRedFlag = (breathing.finished && breathing.taps > MAX_RESTING_BPM)
      || (breathing.running && breathing.taps > MAX_RESTING_BPM)
      || breathing.retractions || breathing.flaring || breathing.grunting;
    const anyRedFlag = tempRedFlag || breathRedFlag;

    return `
      <h1 class="serif-title" style="font-size:24px;">Digestion, Vitals & Safety</h1>
      <p class="small muted" style="margin-bottom:14px;">Diaper output, 60-sec breathing checker, fever/hypothermia alert & elder-friendly rules.</p>

      <!-- 1. Diaper & Digestion Card -->
      <section class="card">
        <div style="display:flex;justify-content:space-between;align-items:center;gap:10px;margin-bottom:10px;">
          <div>
            <h2 style="font-size:17px;">24-Hr Digestion & Diapers</h2>
            <div class="small" style="color:var(--primary);font-weight:700;">
              ${m.wet24h} wet (target ≥ 6) • ${m.stools24h} stools in last 24h
            </div>
          </div>
          <button type="button" class="btn btn-sm btn-primary" id="btn-safety-log-dig">+ Log</button>
        </div>
        <div style="display:flex;flex-direction:column;gap:6px;">
          ${DIGESTION_TIPS.map((t) => `<div class="small muted">• ${escHtml(t)}</div>`).join('')}
        </div>
      </section>

      <!-- 2. 60-Second Tap-to-Count Resting Breathing & Temperature Checker -->
      <section class="card ${anyRedFlag ? 'card-error' : ''}">
        <h2 style="font-size:17px;margin-bottom:4px;">60-Sec Resting Breathing Checker</h2>
        <p class="small muted" style="margin-bottom:12px;">Count while Charvik is asleep with a clear nose. Immediate alert if <strong>&gt; 60 breaths/min</strong> or any distress sign is checked.</p>

        <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:12px;">
          <div>
            <div class="mono" style="font-size:28px;font-weight:800;">${breathing.taps} breaths</div>
            <div class="small muted mono">
              ${breathing.running ? `${breathing.remainingSec}s left (~${estBpm} bpm)` : breathing.finished ? `Final rate: ${breathing.taps} bpm` : 'Ready (60-sec window)'}
            </div>
          </div>
          ${!breathing.running
            ? `<button type="button" class="btn btn-sm btn-primary" id="btn-start-breathing">${breathing.finished ? 'Recount' : 'Start 60s'}</button>`
            : `<button type="button" class="btn btn-sm btn-outline" id="btn-reset-breathing">Reset</button>`
          }
        </div>

        ${breathing.running ? `
          <button type="button" class="btn btn-secondary btn-huge" id="btn-tap-breath" style="margin-bottom:12px;">
            TAP ON EACH BREATH (${breathing.taps})
          </button>
        ` : ''}

        <label class="checkbox-row">
          <input type="checkbox" id="chk-retractions" ${breathing.retractions ? 'checked' : ''} />
          <span>Rib retractions (skin pulling in under/between ribs)</span>
        </label>
        <label class="checkbox-row">
          <input type="checkbox" id="chk-flaring" ${breathing.flaring ? 'checked' : ''} />
          <span>Nasal flaring (nostrils widening on every breath)</span>
        </label>
        <label class="checkbox-row">
          <input type="checkbox" id="chk-grunting" ${breathing.grunting ? 'checked' : ''} />
          <span>Grunting sound on exhale</span>
        </label>

        <hr style="border:none;border-top:1px solid var(--outline-soft);margin:14px 0;" />

        <!-- Temperature Checker -->
        <h2 style="font-size:17px;margin-bottom:6px;">Temperature Checker</h2>
        <div class="mono" style="font-size:20px;font-weight:800;color:${tempRedFlag ? 'var(--error)' : 'var(--ink)'};">
          ${tempF.toFixed(1)}°F (${tempC.toFixed(1)}°C)
        </div>
        <input type="range" id="temp-slider" class="range-slider" min="95.0" max="104.0" step="0.1" value="${tempF.toFixed(1)}" />
        <div class="xs muted" style="margin-bottom:12px;">
          Doctor red-flag thresholds: <strong>&gt; 100.4°F (38.0°C)</strong> or <strong>&lt; 97.7°F (36.5°C)</strong>
        </div>

        ${anyRedFlag ? `
          <div style="background:var(--error);color:var(--on-error);padding:12px;border-radius:14px;font-weight:800;font-size:13px;margin-bottom:12px;">
            🚨 IMMEDIATE DOCTOR ALERT: Red-flag breathing (&gt;60 bpm / retractions / flaring / grunting) or abnormal temperature detected. Contact Apollo Pediatrics / emergency care immediately.
          </div>
        ` : ''}

        <button type="button" class="btn btn-primary" id="btn-save-vitals">Save Vitals Check</button>
        ${state.vitalsLogs.length ? `
          <div class="xs muted" style="margin-top:8px;">
            Last check: ${state.vitalsLogs[0].tempF.toFixed(1)}°F • ${state.vitalsLogs[0].bpm} bpm (${formatTime(state.vitalsLogs[0].ts)})
          </div>
        ` : ''}
      </section>

      <!-- 3. Elder-Friendly Sleep Safety & Restricted Items -->
      <div class="section-title serif-title">Sleep Safety & Restricted Items</div>
      <p class="small muted" style="margin-bottom:10px;">Clear pediatric guardrails to share with family & elders:</p>
      ${SAFETY_RULES.map((rule) => `
        <div class="safety-item ${rule.restricted ? 'restricted' : 'safe'}">
          <div class="safety-icon">${rule.restricted ? '🚫' : '🛡️'}</div>
          <div>
            <div style="font-weight:800;font-size:14px;margin-bottom:2px;">${escHtml(rule.title)}</div>
            <div class="small">${escHtml(rule.detail)}</div>
          </div>
        </div>
      `).join('')}
    `;
  }

  // ---------- Screen 4: Apollo Vaccines & Alarms ----------
  function renderVaccinesScreen() {
    const doneCount = state.vaccines.filter((v) => v.done).length;
    const filtered = state.vaccines.filter((v) => {
      if (vaccineFilter === 'pending') return !v.done;
      if (vaccineFilter === 'done') return v.done;
      return true;
    });

    return `
      <h1 class="serif-title" style="font-size:24px;">Apollo Vaccines & Alarms</h1>
      <p class="small muted" style="margin-bottom:14px;">IAP pediatric schedule anchored to Oct 26, 2026 • ${doneCount}/${state.vaccines.length} doses completed</p>

      <!-- Alarms & Apple Calendar Export Card -->
      <section class="card">
        <div style="display:flex;justify-content:space-between;align-items:center;gap:8px;margin-bottom:10px;">
          <h2 style="font-size:17px;">🔔 Alarms & iPhone Alerts</h2>
          <button type="button" class="btn btn-sm btn-tonal" id="btn-open-add-alarm">+ Add</button>
        </div>

        <div class="btn-row">
          <button type="button" class="btn btn-outline" id="btn-test-alarm">
            ${isRinging ? '🔇 Stop Test' : '🔊 Test Alarm Sound'}
          </button>
          <button type="button" class="btn btn-primary" id="btn-export-ics">
            📅 Add to Apple Calendar
          </button>
        </div>
        <p class="xs muted" style="margin-bottom:10px;">
          Tip for iPhone: Tap <strong>Add to Apple Calendar (.ics)</strong> so iOS fires native lock-screen notifications for all Apollo vaccination dates & daily 7:30 AM reminders even when Safari is closed!
        </p>

        ${state.alarms.map((a) => `
          <div class="list-row">
            <div style="flex:1;">
              <div class="mono" style="font-weight:800;font-size:14px;">
                ${formatClock(a.hour, a.minute)} ${a.repeatDaily ? '• Daily' : '• One-shot'}
              </div>
              <div class="small muted">${escHtml(a.label)}</div>
            </div>
            <label class="switch" aria-label="Toggle alarm">
              <input type="checkbox" data-toggle-alarm="${a.id}" ${a.enabled ? 'checked' : ''} />
              <span class="slider-toggle"></span>
            </label>
            <button type="button" class="del-btn" data-del-alarm="${a.id}" aria-label="Delete alarm">🗑️</button>
          </div>
        `).join('')}
      </section>

      <!-- Filter Chips -->
      <div class="chip-row">
        <button type="button" class="chip ${vaccineFilter === 'pending' ? 'active' : ''}" data-vax-filter="pending">Due & Upcoming</button>
        <button type="button" class="chip ${vaccineFilter === 'done' ? 'active' : ''}" data-vax-filter="done">Completed (${doneCount})</button>
        <button type="button" class="chip ${vaccineFilter === 'all' ? 'active' : ''}" data-vax-filter="all">All (${state.vaccines.length})</button>
      </div>

      <!-- Vaccine Cards -->
      ${filtered.map((v) => {
        const dUntil = daysUntilIso(v.dueDateIso);
        const dueSoon = !v.done && dUntil >= -7 && dUntil <= 21;
        const badgeText = v.done
          ? 'Done ✓'
          : dUntil < 0
            ? `Overdue ${Math.abs(dUntil)}d`
            : dUntil === 0
              ? 'Due Today'
              : `In ${dUntil}d`;
        return `
          <section class="card ${dueSoon ? 'card-secondary' : ''}" style="padding:14px;">
            <div style="display:flex;justify-content:space-between;align-items:flex-start;gap:8px;">
              <div>
                <div style="font-weight:800;font-size:15px;">${v.done ? '✅' : '💉'} ${escHtml(v.name)}</div>
                <div class="small muted">${escHtml(v.stage)} • Due ${escHtml(formatDisplayDate(v.dueDateIso))}</div>
              </div>
              <span class="pill ${v.done ? '' : dueSoon ? 'pill-error' : 'pill-amber'}">${badgeText}</span>
            </div>
            <div class="xs muted" style="margin-top:6px;">${escHtml(v.purpose)}</div>
            ${v.hospitalNote ? `
              <div class="xs" style="color:var(--primary);font-weight:700;margin-top:4px;">
                Note: ${escHtml(v.hospitalNote)} (${escHtml(v.administeredDateIso || v.dueDateIso)})
              </div>
            ` : ''}
            <div style="display:flex;justify-content:flex-end;margin-top:10px;">
              <button type="button" class="btn btn-sm btn-tonal" data-edit-vax="${v.code}">
                ${v.done ? 'Edit Record' : 'Mark Given'}
              </button>
            </div>
          </section>
        `;
      }).join('')}

      <!-- Offline Storage & Backup Card -->
      <section class="card card-muted">
        <h3 style="font-size:15px;margin-bottom:4px;">🔒 100% Offline & On-Device</h3>
        <p class="small muted" style="margin-bottom:10px;">All of Charvik’s logs stay strictly on this phone using browser localStorage — no Firebase, no login, no cloud or AI API.</p>
        <div class="btn-row" style="margin-bottom:0;">
          <button type="button" class="btn btn-sm btn-outline" id="btn-export-backup" style="width:100%;">Export JSON Backup</button>
          <label class="btn btn-sm btn-outline" style="width:100%;cursor:pointer;">
            Restore JSON
            <input type="file" id="input-import-backup" accept="application/json" style="display:none;" />
          </label>
        </div>
      </section>
    `;
  }

  // ---------- Modals ----------
  function openModal(html) {
    const root = document.getElementById('modal-root');
    root.innerHTML = `<div class="modal-sheet"><div class="sheet-handle"></div>${html}</div>`;
    root.classList.remove('hidden');
  }

  function closeModal() {
    const root = document.getElementById('modal-root');
    root.classList.add('hidden');
    root.innerHTML = '';
  }

  function openFeedModal() {
    const targets = computeFeedTargets(state.profile.weightKg);
    const defaultBottle = targets.unlocked ? 65 : 50;
    openModal(`
      <div class="modal-header">
        <h2 class="serif-title" style="font-size:22px;">Log Charvik’s Feed</h2>
        <button type="button" class="del-btn" data-close-modal>✕</button>
      </div>
      <p class="small muted" style="margin-bottom:12px;">
        Protocol: Breast first (10–15 min) → Paced bottle with slow-flow (SS / 0m+) horizontal nipple. Target: <strong>${targets.perFeedMin}–${targets.perFeedMax} ml/feed</strong>.
      </p>

      <div class="field">
        <label>Feed Type</label>
        <select id="feed-type" class="select">
          <option value="Breast + Paced Bottle">Breast First + Paced Bottle Top-Up</option>
          <option value="Paced Bottle (EBM / Formula)">Paced Bottle (Slow-Flow SS/0m+ Horizontal)</option>
          <option value="Direct Breastfeed">Direct Breastfeed Only</option>
        </select>
      </div>

      <div class="field">
        <label>Breastfeeding Duration: <span class="mono" id="feed-breast-val">12 min</span></label>
        <input type="range" id="feed-breast-min" class="range-slider" min="0" max="30" step="1" value="12" />
      </div>

      <div class="field">
        <label>Paced Bottle Volume: <span class="mono" id="feed-bottle-val">${defaultBottle} ml</span></label>
        <input type="range" id="feed-bottle-ml" class="range-slider" min="0" max="120" step="5" value="${defaultBottle}" />
      </div>

      <label class="checkbox-row">
        <input type="checkbox" id="feed-burp" checked />
        <span>Mid-feed burp done at 25–30 ml</span>
      </label>
      <label class="checkbox-row">
        <input type="checkbox" id="feed-saline" />
        <span>Noblok saline drops given 10 min pre-feed</span>
      </label>
      <label class="checkbox-row">
        <input type="checkbox" id="feed-auto-reflux" checked />
        <span>Auto-start 25-min 45° upright reflux timer</span>
      </label>

      <div class="field" style="margin-top:8px;">
        <label for="feed-note">Optional Note</label>
        <input type="text" id="feed-note" class="input" placeholder="e.g., Good latch, calm feed" />
      </div>

      <button type="button" class="btn btn-primary" id="btn-submit-feed">Save Feed</button>
    `);

    document.getElementById('feed-breast-min').addEventListener('input', (e) => {
      document.getElementById('feed-breast-val').textContent = `${e.target.value} min`;
    });
    document.getElementById('feed-bottle-ml').addEventListener('input', (e) => {
      document.getElementById('feed-bottle-val').textContent = `${e.target.value} ml`;
    });
    document.getElementById('btn-submit-feed').addEventListener('click', () => {
      const feedTypeLabel = document.getElementById('feed-type').value;
      const breastMin = Number(document.getElementById('feed-breast-min').value) || 0;
      const bottleMl = Number(document.getElementById('feed-bottle-ml').value) || 0;
      const burpedMidFeed = document.getElementById('feed-burp').checked;
      const salinePreFeed = document.getElementById('feed-saline').checked;
      const autoReflux = document.getElementById('feed-auto-reflux').checked;
      const note = document.getElementById('feed-note').value.trim();

      state.feeds.unshift({
        id: `feed_${Date.now()}`,
        feedTypeLabel,
        breastMin,
        bottleMl,
        burpedMidFeed,
        salinePreFeed,
        note,
        ts: Date.now(),
      });
      if (salinePreFeed) {
        state.suctions.unshift({ id: `suc_${Date.now()}`, dateKey: todayIso(), ts: Date.now() });
      }
      if (autoReflux) {
        startRefluxTimer(25);
      } else {
        saveState();
      }
      closeModal();
      render();
      showToast(autoReflux ? 'Feed logged • 25m reflux hold started' : 'Feed logged');
    });
  }

  function openDigestionModal() {
    openModal(`
      <div class="modal-header">
        <h2 class="serif-title" style="font-size:22px;">Log Diaper & Digestion</h2>
        <button type="button" class="del-btn" data-close-modal>✕</button>
      </div>

      <label class="checkbox-row">
        <input type="checkbox" id="dig-wet" checked />
        <span>Wet diaper (target ≥ 6 in 24 hrs)</span>
      </label>
      <label class="checkbox-row">
        <input type="checkbox" id="dig-stool" />
        <span>Stool passed</span>
      </label>

      <div class="field" style="margin-top:6px;">
        <label for="dig-stool-color">Stool Color / Consistency</label>
        <select id="dig-stool-color" class="select">
          <option value="Mustard Yellow (Seedy/Soft — Normal)">Mustard Yellow (Seedy/Soft — Normal)</option>
          <option value="Yellow-Green (Soft)">Yellow-Green (Soft)</option>
          <option value="Watery / Loose">Watery / Loose</option>
          <option value="Pasty / Thick">Pasty / Thick</option>
          <option value="Pale / White / Bloody (Call Doctor)">Pale / White / Bloody (Call Doctor)</option>
        </select>
      </div>

      <div class="field">
        <label for="dig-reflux">Silent Reflux / Mucus / Spit-up</label>
        <select id="dig-reflux" class="select">
          <option value="None">None</option>
          <option value="Mild silent reflux / throat mucus">Mild silent reflux / throat mucus</option>
          <option value="Small curdled spit-up">Small curdled spit-up</option>
          <option value="Large spit-up / forceful">Large spit-up / forceful</option>
        </select>
      </div>

      <label class="checkbox-row">
        <input type="checkbox" id="dig-hiccups" />
        <span>Immature diaphragm hiccups</span>
      </label>
      <label class="checkbox-row">
        <input type="checkbox" id="dig-gas" />
        <span>Gas / straining / tummy discomfort</span>
      </label>

      <div class="field" style="margin-top:8px;">
        <label for="dig-note">Notes</label>
        <input type="text" id="dig-note" class="input" placeholder="Optional note" />
      </div>

      <button type="button" class="btn btn-primary" id="btn-submit-dig">Save Diaper & Digestion Log</button>
    `);

    document.getElementById('btn-submit-dig').addEventListener('click', () => {
      const wetDiaper = document.getElementById('dig-wet').checked;
      const stoolPassed = document.getElementById('dig-stool').checked;
      const stoolColor = document.getElementById('dig-stool-color').value;
      const reflux = document.getElementById('dig-reflux').value;
      const hiccups = document.getElementById('dig-hiccups').checked;
      const gas = document.getElementById('dig-gas').checked;
      const note = document.getElementById('dig-note').value.trim();

      state.digestionLogs.unshift({
        id: `dig_${Date.now()}`,
        wetDiaper,
        stoolPassed,
        stoolColor,
        reflux,
        hiccups,
        gas,
        note,
        ts: Date.now(),
      });
      saveState();
      closeModal();
      render();
      showToast('Diaper & digestion log saved');
    });
  }

  function openWeightModal() {
    openModal(`
      <div class="modal-header">
        <h2 class="serif-title" style="font-size:22px;">Update Charvik’s Weight</h2>
        <button type="button" class="del-btn" data-close-modal>✕</button>
      </div>
      <p class="small muted" style="margin-bottom:12px;">
        Expected gain: <strong>20–30 g/day</strong>. Daily feed volume updates automatically (150–180 ml × kg) and unlocks 60–70 ml/feed at ≥ 3.00 kg.
      </p>
      <div class="field">
        <label for="weight-input">Current Weight (kg)</label>
        <input type="number" id="weight-input" class="input mono" step="0.01" min="1.5" max="15" value="${state.profile.weightKg.toFixed(2)}" />
      </div>
      <div id="weight-preview" class="card card-primary" style="padding:12px;margin-bottom:14px;"></div>
      <div class="field">
        <label for="weight-note">Note (optional)</label>
        <input type="text" id="weight-note" class="input" placeholder="e.g., Apollo clinic scale" />
      </div>
      <button type="button" class="btn btn-primary" id="btn-submit-weight">Save Weight</button>
    `);

    const updatePreview = () => {
      const val = parseFloat(document.getElementById('weight-input').value) || state.profile.weightKg;
      const t = computeFeedTargets(val);
      document.getElementById('weight-preview').innerHTML = `
        <div class="mono" style="font-weight:800;">Daily target: ${t.dailyMin}–${t.dailyMax} ml/day</div>
        <div class="small">Per feed: ${t.perFeedMin}–${t.perFeedMax} ml ${t.unlocked ? '• 🎉 3.0 kg stage unlocked!' : '• Unlocks 60–70 ml at 3.00 kg'}</div>
      `;
    };
    updatePreview();
    document.getElementById('weight-input').addEventListener('input', updatePreview);

    document.getElementById('btn-submit-weight').addEventListener('click', () => {
      const val = parseFloat(document.getElementById('weight-input').value);
      if (!val || val < 1.5 || val > 15) {
        showToast('Enter a valid weight between 1.5 and 15 kg');
        return;
      }
      state.profile.weightKg = Number(val.toFixed(2));
      state.weightHistory.unshift({
        id: `w_${Date.now()}`,
        weightKg: state.profile.weightKg,
        note: document.getElementById('weight-note').value.trim(),
        ts: Date.now(),
      });
      saveState();
      closeModal();
      render();
      showToast(state.profile.weightKg >= 3.0
        ? `Weight ${state.profile.weightKg.toFixed(2)} kg saved — 60–70 ml/feed unlocked!`
        : `Weight ${state.profile.weightKg.toFixed(2)} kg saved`);
    });
  }

  function openAddAlarmModal() {
    openModal(`
      <div class="modal-header">
        <h2 class="serif-title" style="font-size:22px;">Add Care Alarm</h2>
        <button type="button" class="del-btn" data-close-modal>✕</button>
      </div>
      <div class="field">
        <label for="alarm-label">Reminder Label</label>
        <input type="text" id="alarm-label" class="input" value="Charvik Feed / Vitamin D3 Reminder" />
      </div>
      <div class="field">
        <label for="alarm-time">Time</label>
        <input type="time" id="alarm-time" class="input mono" value="08:00" />
      </div>
      <label class="checkbox-row">
        <input type="checkbox" id="alarm-daily" checked />
        <span>Repeat every day</span>
      </label>
      <button type="button" class="btn btn-primary" id="btn-submit-alarm" style="margin-top:10px;">Save Alarm</button>
    `);

    document.getElementById('btn-submit-alarm').addEventListener('click', () => {
      const label = document.getElementById('alarm-label').value.trim() || 'Charvik Care Reminder';
      const [hStr, mStr] = (document.getElementById('alarm-time').value || '08:00').split(':');
      const hour = Number(hStr) || 0;
      const minute = Number(mStr) || 0;
      const repeatDaily = document.getElementById('alarm-daily').checked;
      state.alarms.push({
        id: `alm_${Date.now()}`,
        label,
        hour,
        minute,
        repeatDaily,
        enabled: true,
        category: 'CUSTOM',
        lastFiredDate: '',
      });
      saveState();
      closeModal();
      render();
      showToast(`Alarm set for ${formatClock(hour, minute)}`);
    });
  }

  function openVaccineModal(code) {
    const vax = state.vaccines.find((v) => v.code === code);
    if (!vax) return;
    openModal(`
      <div class="modal-header">
        <h2 class="serif-title" style="font-size:20px;">${escHtml(vax.name)}</h2>
        <button type="button" class="del-btn" data-close-modal>✕</button>
      </div>
      <p class="small muted" style="margin-bottom:10px;">${escHtml(vax.stage)} • Due ${escHtml(formatDisplayDate(vax.dueDateIso))}</p>
      <label class="checkbox-row">
        <input type="checkbox" id="vax-done" ${vax.done ? 'checked' : 'checked'} />
        <span>Mark dose as Completed / Given</span>
      </label>
      <div class="field" style="margin-top:8px;">
        <label for="vax-date">Date Given</label>
        <input type="date" id="vax-date" class="input mono" value="${escHtml(vax.administeredDateIso || todayIso())}" />
      </div>
      <div class="field">
        <label for="vax-note">Apollo Hospital / Pediatrician / Brand Note</label>
        <textarea id="vax-note" class="textarea" placeholder="e.g., Apollo Hospitals Pediatrics, Dr. ...">${escHtml(vax.hospitalNote || 'Apollo Hospitals Pediatrics')}</textarea>
      </div>
      <button type="button" class="btn btn-primary" id="btn-submit-vax">Save Vaccine Record</button>
    `);

    // Set initial checked state properly
    document.getElementById('vax-done').checked = true;

    document.getElementById('btn-submit-vax').addEventListener('click', () => {
      vax.done = document.getElementById('vax-done').checked;
      vax.administeredDateIso = document.getElementById('vax-date').value || todayIso();
      vax.hospitalNote = document.getElementById('vax-note').value.trim();
      saveState();
      closeModal();
      render();
      showToast(`${vax.name} updated`);
    });
  }

  // ---------- Timer Actions ----------
  function startRefluxTimer(minutes) {
    const dur = Math.max(1, minutes) * 60;
    state.timers.reflux = {
      running: true,
      durationSec: dur,
      endAtMs: Date.now() + dur * 1000,
      selectedMin: minutes,
      completedAlert: false,
    };
    saveState();
    unlockAudio(false);
    render();
    showToast(`${minutes}-min upright reflux hold started`);
  }

  function stopRefluxTimer() {
    state.timers.reflux.running = false;
    state.timers.reflux.completedAlert = false;
    state.timers.reflux.endAtMs = 0;
    stopLoopingAlarm();
    saveState();
    render();
  }

  function startPumpTimer(minutes) {
    const dur = Math.max(5, minutes) * 60;
    state.timers.pump = {
      ...state.timers.pump,
      running: true,
      durationSec: dur,
      startAtMs: Date.now(),
      selectedMin: minutes,
      phaseChimePlayed: false,
      completedAlert: false,
    };
    saveState();
    unlockAudio(false);
    render();
    showToast(`Pumping started — 2m stimulation then expression`);
  }

  function stopPumpTimer() {
    state.timers.pump.running = false;
    state.timers.pump.completedAlert = false;
    stopLoopingAlarm();
    saveState();
    render();
  }

  // ---------- RFC-5545 Apple Calendar (.ics) Export ----------
  function exportAppleCalendarIcs() {
    const pad2 = (n) => String(n).padStart(2, '0');
    const now = new Date();
    const dtStamp = `${now.getUTCFullYear()}${pad2(now.getUTCMonth() + 1)}${pad2(now.getUTCDate())}T${pad2(now.getUTCHours())}${pad2(now.getUTCMinutes())}${pad2(now.getUTCSeconds())}Z`;

    const escapeIcs = (str) => String(str || '').replace(/\\/g, '\\\\').replace(/;/g, '\\;').replace(/,/g, '\\,').replace(/\n/g, '\\n');

    const lines = [
      'BEGIN:VCALENDAR',
      'VERSION:2.0',
      'PRODID:-//Charvik Newborn Care//Offline PWA//EN',
      'CALSCALE:GREGORIAN',
      'X-WR-CALNAME:Charvik Care & Apollo Vaccines',
    ];

    // 1. Upcoming Apollo vaccinations (grouped neatly or individual 9:00 AM events)
    state.vaccines.filter((v) => !v.done).forEach((v) => {
      const ymd = v.dueDateIso.replace(/-/g, '');
      lines.push(
        'BEGIN:VEVENT',
        `UID:charvik-vax-${v.code}@charvik.local`,
        `DTSTAMP:${dtStamp}`,
        `DTSTART:${ymd}T090000`,
        `DTEND:${ymd}T093000`,
        `SUMMARY:${escapeIcs(`Charvik Vaccine: ${v.name} (${v.stage})`)}`,
        `DESCRIPTION:${escapeIcs(`Apollo Hospitals / IAP Schedule — ${v.purpose}. DOB: Oct 26, 2026.`)}`,
        'BEGIN:VALARM',
        'TRIGGER:-P1D',
        'ACTION:DISPLAY',
        `DESCRIPTION:${escapeIcs(`Tomorrow: ${v.name} for Charvik at Apollo Hospitals`)}`,
        'END:VALARM',
        'BEGIN:VALARM',
        'TRIGGER:PT0M',
        'ACTION:DISPLAY',
        `DESCRIPTION:${escapeIcs(`Today 9 AM: ${v.name} (${v.stage}) for Charvik`)}`,
        'END:VALARM',
        'END:VEVENT'
      );
    });

    // 2. Enabled daily care alarms (e.g., 7:30 AM Morning Sun & Vitamin D3)
    const todayYmd = todayIso().replace(/-/g, '');
    state.alarms.filter((a) => a.enabled).forEach((a) => {
      const hh = pad2(a.hour);
      const mm = pad2(a.minute);
      const endMin = (a.minute + 15) % 60;
      const endHour = (a.hour + Math.floor((a.minute + 15) / 60)) % 24;
      lines.push(
        'BEGIN:VEVENT',
        `UID:charvik-alarm-${a.id}@charvik.local`,
        `DTSTAMP:${dtStamp}`,
        `DTSTART:${todayYmd}T${hh}${mm}00`,
        `DTEND:${todayYmd}T${pad2(endHour)}${pad2(endMin)}00`,
        ...(a.repeatDaily ? ['RRULE:FREQ=DAILY'] : []),
        `SUMMARY:${escapeIcs(`Charvik: ${a.label}`)}`,
        `DESCRIPTION:${escapeIcs('Daily care reminder from Charvik offline tracker.')}`,
        'BEGIN:VALARM',
        'TRIGGER:PT0M',
        'ACTION:DISPLAY',
        `DESCRIPTION:${escapeIcs(`Charvik Reminder: ${a.label}`)}`,
        'END:VALARM',
        'END:VEVENT'
      );
    });

    lines.push('END:VCALENDAR');
    const icsContent = lines.join('\r\n');
    const blob = new Blob([icsContent], { type: 'text/calendar;charset=utf-8' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = 'Charvik-Care-Apollo-Schedule.ics';
    document.body.appendChild(link);
    link.click();
    link.remove();
    setTimeout(() => URL.revokeObjectURL(url), 5000);
    showToast('Calendar (.ics) exported — open to add native iOS alerts');
  }

  // ---------- Tick Loop (Wall-Clock Accurate Even After Screen Lock) ----------
  function tickTimersAndAlarms() {
    const now = Date.now();
    let needsRender = false;

    // 1. Reflux Hold Timer
    const reflux = state.timers.reflux;
    if (reflux.running) {
      if (now >= reflux.endAtMs) {
        reflux.running = false;
        reflux.completedAlert = true;
        saveState();
        startLoopingAlarm('Reflux Upright Hold Complete — Safe to lay Charvik flat on his back');
      }
      if (activeTab === 'today' || activeTab === 'timers') needsRender = true;
    }

    // 2. Two-Phase Pumping Timer
    const pump = state.timers.pump;
    if (pump.running) {
      const elapsed = Math.floor((now - pump.startAtMs) / 1000);
      if (elapsed >= 120 && !pump.phaseChimePlayed) {
        pump.phaseChimePlayed = true;
        saveState();
        playPhaseChime();
        showToast('2:00 Stimulation complete → Switch to Pain-Free Expression');
      }
      if (elapsed >= pump.durationSec) {
        pump.running = false;
        pump.completedAlert = true;
        saveState();
        startLoopingAlarm(`Pumping Session Complete (${pump.selectedMin} min)`);
      }
      if (activeTab === 'timers') needsRender = true;
    }

    // 3. Breathing 60s Counter
    if (breathing.running) {
      const elapsed = Math.floor((now - breathing.startedAtMs) / 1000);
      const rem = Math.max(0, 60 - elapsed);
      if (rem !== breathing.remainingSec) {
        breathing.remainingSec = rem;
        if (rem === 0) {
          breathing.running = false;
          breathing.finished = true;
          if (breathing.taps > MAX_RESTING_BPM || breathing.retractions || breathing.flaring || breathing.grunting) {
            startLoopingAlarm(`RED-FLAG BREATHING ALERT (${breathing.taps} bpm) — Call Doctor`);
          } else {
            playPhaseChime();
            showToast(`Breathing check complete: ${breathing.taps} breaths/min`);
          }
        }
        if (activeTab === 'safety') needsRender = true;
      }
    }

    // 4. Snoozed Alarm
    if (state.timers.snoozeUntilMs && now >= state.timers.snoozeUntilMs) {
      const title = state.timers.snoozeTitle || 'Snoozed Care Alarm';
      state.timers.snoozeUntilMs = 0;
      state.timers.snoozeTitle = '';
      saveState();
      startLoopingAlarm(title);
    }

    // 5. Wall-clock Daily Alarms (fires while app is active)
    const dt = new Date(now);
    const todayKey = todayIso();
    state.alarms.forEach((a) => {
      if (!a.enabled) return;
      if (a.hour === dt.getHours() && a.minute === dt.getMinutes() && a.lastFiredDate !== todayKey) {
        a.lastFiredDate = todayKey;
        if (!a.repeatDaily) a.enabled = false;
        saveState();
        startLoopingAlarm(a.label);
        if (activeTab === 'vaccines') needsRender = true;
      }
    });

    // 6. Refresh live "since last feed" every 30s on Today tab
    if (activeTab === 'today' && dt.getSeconds() % 30 === 0) {
      needsRender = true;
    }

    if (needsRender) render();
  }

  // ---------- Event Binding ----------
  function bindScreenEvents() {
    const root = document.getElementById('screen-root');

    // Today bindings
    root.querySelector('#btn-open-feed')?.addEventListener('click', openFeedModal);
    root.querySelector('#btn-open-digestion')?.addEventListener('click', openDigestionModal);
    root.querySelector('#btn-open-weight')?.addEventListener('click', openWeightModal);
    root.querySelector('#btn-quick-weight')?.addEventListener('click', openWeightModal);
    root.querySelector('#btn-stop-reflux-today')?.addEventListener('click', stopRefluxTimer);
    root.querySelector('#btn-quick-reflux')?.addEventListener('click', () => {
      if (state.timers.reflux.running) {
        switchTab('timers');
      } else {
        startRefluxTimer(25);
      }
    });

    root.querySelectorAll('[data-routine]').forEach((btn) => {
      btn.addEventListener('click', () => {
        const key = btn.getAttribute('data-routine');
        const todayKey = todayIso();
        const list = state.routineChecks[todayKey] || [];
        const idx = list.indexOf(key);
        if (idx >= 0) list.splice(idx, 1);
        else list.push(key);
        state.routineChecks[todayKey] = list;
        saveState();
        render();
      });
    });

    root.querySelectorAll('[data-del-feed]').forEach((btn) => {
      btn.addEventListener('click', () => {
        const id = btn.getAttribute('data-del-feed');
        state.feeds = state.feeds.filter((f) => f.id !== id);
        saveState();
        render();
        showToast('Feed entry removed');
      });
    });

    root.querySelectorAll('[data-del-dig]').forEach((btn) => {
      btn.addEventListener('click', () => {
        const id = btn.getAttribute('data-del-dig');
        state.digestionLogs = state.digestionLogs.filter((d) => d.id !== id);
        saveState();
        render();
        showToast('Digestion log removed');
      });
    });

    // Timers bindings
    root.querySelector('#btn-timers-unlock-audio')?.addEventListener('click', () => unlockAudio(true));
    root.querySelectorAll('[data-reflux-min]').forEach((btn) => {
      btn.addEventListener('click', () => {
        state.timers.reflux.selectedMin = Number(btn.getAttribute('data-reflux-min'));
        saveState();
        render();
      });
    });
    root.querySelector('#btn-start-reflux')?.addEventListener('click', () => startRefluxTimer(state.timers.reflux.selectedMin));
    root.querySelector('#btn-stop-reflux')?.addEventListener('click', stopRefluxTimer);

    root.querySelectorAll('[data-pump-min]').forEach((btn) => {
      btn.addEventListener('click', () => {
        state.timers.pump.selectedMin = Number(btn.getAttribute('data-pump-min'));
        saveState();
        render();
      });
    });
    root.querySelector('#btn-start-pump')?.addEventListener('click', () => startPumpTimer(state.timers.pump.selectedMin));
    root.querySelector('#btn-stop-pump')?.addEventListener('click', stopPumpTimer);

    const pumpSlider = root.querySelector('#pump-ml-slider');
    if (pumpSlider) {
      pumpSlider.addEventListener('input', (e) => {
        const val = Number(e.target.value);
        state.timers.pump.pumpedMl = val;
        const valEl = root.querySelector('#pump-ml-val');
        const saveBtn = root.querySelector('#btn-save-pump');
        if (valEl) valEl.textContent = `${val} ml`;
        if (saveBtn) saveBtn.textContent = `💾 Log Pump Session (${val} ml)`;
      });
      pumpSlider.addEventListener('change', saveState);
    }

    root.querySelector('#btn-save-pump')?.addEventListener('click', () => {
      const pump = state.timers.pump;
      const mins = pump.running ? Math.max(1, Math.round((Date.now() - pump.startAtMs) / 60000)) : pump.selectedMin;
      state.pumpSessions.unshift({ id: `pump_${Date.now()}`, minutes: mins, ml: pump.pumpedMl, ts: Date.now() });
      saveState();
      render();
      showToast(`Pump session logged (${mins}m, ${pump.pumpedMl} ml)`);
    });

    root.querySelector('#btn-log-suction')?.addEventListener('click', () => {
      const todayKey = todayIso();
      state.suctions.unshift({ id: `suc_${Date.now()}`, dateKey: todayKey, ts: Date.now() });
      const count = state.suctions.filter((s) => s.dateKey === todayKey).length;
      saveState();
      render();
      showToast(count >= MAX_SUCTIONS
        ? `Nasal suction #${count} — Daily cap (4/day) reached!`
        : `Noblok saline / suction #${count} logged`);
    });

    // Safety bindings
    root.querySelector('#btn-safety-log-dig')?.addEventListener('click', openDigestionModal);
    root.querySelector('#btn-start-breathing')?.addEventListener('click', () => {
      unlockAudio(false);
      breathing = {
        ...breathing,
        running: true,
        startedAtMs: Date.now(),
        remainingSec: 60,
        taps: 0,
        finished: false,
      };
      render();
    });
    root.querySelector('#btn-reset-breathing')?.addEventListener('click', () => {
      breathing = {
        running: false,
        startedAtMs: 0,
        remainingSec: 60,
        taps: 0,
        finished: false,
        retractions: false,
        flaring: false,
        grunting: false,
      };
      stopLoopingAlarm();
      render();
    });
    root.querySelector('#btn-tap-breath')?.addEventListener('click', () => {
      if (!breathing.running) return;
      breathing.taps += 1;
      if (breathing.taps === MAX_RESTING_BPM + 1) {
        startLoopingAlarm('Breathing > 60 breaths/min — Contact Doctor Immediately');
      }
      render();
    });
    root.querySelector('#chk-retractions')?.addEventListener('change', (e) => {
      breathing.retractions = e.target.checked;
      if (e.target.checked) startLoopingAlarm('Rib Retractions Flagged — Contact Doctor');
      render();
    });
    root.querySelector('#chk-flaring')?.addEventListener('change', (e) => {
      breathing.flaring = e.target.checked;
      if (e.target.checked) startLoopingAlarm('Nasal Flaring Flagged — Contact Doctor');
      render();
    });
    root.querySelector('#chk-grunting')?.addEventListener('change', (e) => {
      breathing.grunting = e.target.checked;
      if (e.target.checked) startLoopingAlarm('Grunting Breathing Flagged — Contact Doctor');
      render();
    });

    const tempSlider = root.querySelector('#temp-slider');
    if (tempSlider) {
      tempSlider.addEventListener('input', (e) => {
        tempF = parseFloat(e.target.value);
        render();
      });
    }

    root.querySelector('#btn-save-vitals')?.addEventListener('click', () => {
      const bpm = breathing.finished ? breathing.taps : Math.round((breathing.taps * 60) / Math.max(1, 60 - breathing.remainingSec));
      const redFlag = tempF > FEVER_F || tempF < HYPO_F || bpm > MAX_RESTING_BPM || breathing.retractions || breathing.flaring || breathing.grunting;
      state.vitalsLogs.unshift({ id: `vit_${Date.now()}`, tempF, bpm, redFlag, ts: Date.now() });
      saveState();
      render();
      showToast(redFlag ? 'RED-FLAG VITALS SAVED — Call Doctor Immediately' : `Vitals saved (${tempF.toFixed(1)}°F, ${bpm} bpm)`);
    });

    // Vaccines & Alarms bindings
    root.querySelector('#btn-open-add-alarm')?.addEventListener('click', openAddAlarmModal);
    root.querySelector('#btn-test-alarm')?.addEventListener('click', () => {
      if (isRinging) stopLoopingAlarm();
      else startLoopingAlarm('Test Care Alarm (Charvik)');
      render();
    });
    root.querySelector('#btn-export-ics')?.addEventListener('click', exportAppleCalendarIcs);

    root.querySelectorAll('[data-toggle-alarm]').forEach((input) => {
      input.addEventListener('change', () => {
        const id = input.getAttribute('data-toggle-alarm');
        const alarm = state.alarms.find((a) => a.id === id);
        if (alarm) {
          alarm.enabled = input.checked;
          saveState();
          showToast(alarm.enabled ? 'Alarm enabled' : 'Alarm paused');
        }
      });
    });

    root.querySelectorAll('[data-del-alarm]').forEach((btn) => {
      btn.addEventListener('click', () => {
        const id = btn.getAttribute('data-del-alarm');
        state.alarms = state.alarms.filter((a) => a.id !== id);
        saveState();
        render();
        showToast('Alarm deleted');
      });
    });

    root.querySelectorAll('[data-vax-filter]').forEach((btn) => {
      btn.addEventListener('click', () => {
        vaccineFilter = btn.getAttribute('data-vax-filter');
        render();
      });
    });

    root.querySelectorAll('[data-edit-vax]').forEach((btn) => {
      btn.addEventListener('click', () => openVaccineModal(btn.getAttribute('data-edit-vax')));
    });

    root.querySelector('#btn-export-backup')?.addEventListener('click', () => {
      const blob = new Blob([JSON.stringify(state, null, 2)], { type: 'application/json' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `charvik-backup-${todayIso()}.json`;
      document.body.appendChild(a);
      a.click();
      a.remove();
      setTimeout(() => URL.revokeObjectURL(url), 4000);
      showToast('Backup JSON downloaded');
    });

    root.querySelector('#input-import-backup')?.addEventListener('change', (e) => {
      const file = e.target.files?.[0];
      if (!file) return;
      const reader = new FileReader();
      reader.onload = () => {
        try {
          const parsed = JSON.parse(String(reader.result));
          if (parsed && parsed.profile && Array.isArray(parsed.vaccines)) {
            state = { ...createSeedState(), ...parsed };
            saveState();
            render();
            showToast('Backup restored successfully');
          } else {
            showToast('Invalid backup file');
          }
        } catch {
          showToast('Could not read backup JSON');
        }
      };
      reader.readAsText(file);
    });
  }

  function render() {
    const root = document.getElementById('screen-root');
    if (!root) return;
    if (activeTab === 'today') root.innerHTML = renderTodayScreen();
    else if (activeTab === 'timers') root.innerHTML = renderTimersScreen();
    else if (activeTab === 'safety') root.innerHTML = renderSafetyScreen();
    else root.innerHTML = renderVaccinesScreen();
    bindScreenEvents();
  }

  function switchTab(tab) {
    activeTab = tab;
    document.querySelectorAll('.nav-btn').forEach((btn) => {
      btn.classList.toggle('active', btn.getAttribute('data-tab') === tab);
    });
    window.scrollTo({ top: 0, behavior: 'instant' });
    render();
  }

  // ---------- Bootstrap ----------
  function init() {
    initTheme();
    updateAudioButtonUI();
    renderRingingBanner();

    // iOS Safari Add-to-Home-Screen prompt
    const isIos = /iPad|iPhone|iPod/.test(navigator.userAgent) && !window.MSStream;
    const isStandalone = window.navigator.standalone === true || window.matchMedia('(display-mode: standalone)').matches;
    if (isIos && !isStandalone && !localStorage.getItem(INSTALL_DISMISSED_KEY)) {
      document.getElementById('ios-install-banner')?.classList.remove('hidden');
    }
    document.getElementById('btn-dismiss-install')?.addEventListener('click', () => {
      localStorage.setItem(INSTALL_DISMISSED_KEY, '1');
      document.getElementById('ios-install-banner')?.classList.add('hidden');
    });

    // Global controls
    document.getElementById('btn-theme-toggle')?.addEventListener('click', toggleTheme);
    document.getElementById('btn-audio-unlock')?.addEventListener('click', () => unlockAudio(true));
    document.getElementById('btn-stop-alarm')?.addEventListener('click', () => {
      stopLoopingAlarm();
      render();
    });
    document.getElementById('btn-snooze-alarm')?.addEventListener('click', snoozeLoopingAlarm);

    // Bottom navigation
    document.querySelectorAll('.nav-btn').forEach((btn) => {
      btn.addEventListener('click', () => switchTab(btn.getAttribute('data-tab')));
    });

    // Modal backdrop / close button delegation
    const modalRoot = document.getElementById('modal-root');
    modalRoot?.addEventListener('click', (e) => {
      if (e.target === modalRoot || e.target.closest('[data-close-modal]')) {
        closeModal();
      }
    });

    // Register Offline Service Worker
    if ('serviceWorker' in navigator) {
      window.addEventListener('load', () => {
        navigator.serviceWorker.register('./sw.js').catch(() => {});
      });
    }

    // Resume wall-clock checks immediately on returning from iPhone lock screen
    document.addEventListener('visibilitychange', () => {
      if (document.visibilityState === 'visible') {
        tickTimersAndAlarms();
      }
    });

    render();
    setInterval(tickTimersAndAlarms, 1000);
  }

  init();
})();
