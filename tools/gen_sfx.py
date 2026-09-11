#!/usr/bin/env python3
"""
مولّد أصوات «مين الأطليسي».

كل صوت مربوط بمشهد ومواقيته مأخوذة من الشاشة نفسها — مثلاً ضربة
«بداية الجولة» بتوقع على نفس اللحظة يلي بينزل فيها اسم الجولة. إذا تغيّر
توقيت الحركة بالشاشة، غيّر الرقم هون كمان.

التشغيل:  python3 tools/gen_sfx.py
الخرج:    app/src/main/res/raw/*.wav  (أحادي، ٤٤٫١ كيلوهرتز، ١٦ بت)
"""

import math
import os
import shutil
import subprocess
import wave

import numpy as np

SR = 44100
OUT = os.path.join("app", "src", "main", "res", "raw")

# الأصوات القصيرة جداً بتضل WAV: ترميز MP3 بيزيد سكتة صغيرة بالبداية،
# وبصوت زي لمسة الزر هاي السكتة بتنحسّ تأخير.
KEEP_WAV = {"sfx_tap"}
MP3_KBPS = "128k"


# ── أدوات أساسية ─────────────────────────────────────────────────────

def n(dur):
    return int(dur * SR)


def tline(dur):
    return np.arange(n(dur)) / SR


def buf(dur):
    return np.zeros(n(dur))


def place(dst, sig, at):
    """بتحط [sig] جوّا [dst] عند الثانية [at]."""
    i = n(at)
    if i >= len(dst):
        return dst
    m = min(len(sig), len(dst) - i)
    dst[i:i + m] += sig[:m]
    return dst


def perc(length, attack, decay, curve=3.0):
    """مغلّف نقري: طلعة سريعة ونزلة أُسّية."""
    e = np.ones(length)
    a = max(1, n(attack))
    e[:a] = np.linspace(0.0, 1.0, a) ** 0.6
    d = np.arange(length - a) / max(1, n(decay))
    e[a:] = np.exp(-curve * d)
    return e


def ar(length, attack, release):
    """مغلّف بطلعة ونزلة خطّيتين — للمساحات الهوائية."""
    e = np.ones(length)
    a, r = max(1, n(attack)), max(1, n(release))
    e[:a] = np.linspace(0.0, 1.0, a)
    if r < length:
        e[-r:] = np.linspace(1.0, 0.0, r) ** 1.5
    return e


def lowpass(x, cutoff):
    """مرشّح قطب واحد — بيقصّ الحدّة."""
    a = math.exp(-2.0 * math.pi * cutoff / SR)
    y = np.empty_like(x)
    acc = 0.0
    for i in range(len(x)):
        acc = (1 - a) * x[i] + a * acc
        y[i] = acc
    return y


def lowpass_fast(x, cutoff):
    """نفس الفكرة بس بالتردد — أسرع بكتير عالمقاطع الطويلة."""
    spec = np.fft.rfft(x)
    f = np.fft.rfftfreq(len(x), 1 / SR)
    spec *= 1.0 / (1.0 + (f / max(cutoff, 1.0)) ** 2)
    return np.fft.irfft(spec, len(x))


def highpass_fast(x, cutoff):
    spec = np.fft.rfft(x)
    f = np.fft.rfftfreq(len(x), 1 / SR)
    r = (f / max(cutoff, 1.0)) ** 2
    spec *= r / (1.0 + r)
    return np.fft.irfft(spec, len(x))


def noise(dur, seed=0):
    rng = np.random.default_rng(seed)
    return rng.uniform(-1.0, 1.0, n(dur))


def chirp(f0, f1, dur, curve=1.0):
    """كنسة تردد من [f0] لـ[f1]."""
    t = tline(dur)
    p = (t / max(t[-1], 1e-9)) ** curve
    freq = f0 + (f1 - f0) * p
    return np.sin(2 * np.pi * np.cumsum(freq) / SR)


def comb(x, delay, gain, repeats=4):
    """صدى قصير — بيعطي إحساس القاعة بدون ما يوسّخ الصوت."""
    y = x.copy()
    d = n(delay)
    if d <= 0:
        return y
    for k in range(1, repeats + 1):
        g = gain ** k
        i = d * k
        if i >= len(y):
            break
        y[i:] += g * x[:len(y) - i]
    return y


def norm(x, peak=0.89):
    m = np.max(np.abs(x))
    return x if m < 1e-9 else x * (peak / m)


def softclip(x, drive=1.4):
    return np.tanh(x * drive) / math.tanh(drive)


def fade_edges(x, ms=6):
    """يمنع الطقّة على أول وآخر الملف."""
    k = max(1, n(ms / 1000.0))
    if len(x) > 2 * k:
        x[:k] *= np.linspace(0, 1, k)
        x[-k:] *= np.linspace(1, 0, k)
    return x


def ffmpeg_exe():
    """بيدوّر على ffmpeg — بالنظام أو الجاي مع imageio-ffmpeg."""
    found = shutil.which("ffmpeg")
    if found:
        return found
    try:
        import imageio_ffmpeg
        return imageio_ffmpeg.get_ffmpeg_exe()
    except Exception:
        return None


def save(name, x, ff=None):
    x = fade_edges(norm(softclip(x)))
    data = (np.clip(x, -1.0, 1.0) * 32767.0).astype("<i2")
    wav_path = os.path.join(OUT, name + ".wav")
    with wave.open(wav_path, "wb") as w:
        w.setnchannels(1)
        w.setsampwidth(2)
        w.setframerate(SR)
        w.writeframes(data.tobytes())

    path = wav_path
    if ff and name not in KEEP_WAV:
        mp3_path = os.path.join(OUT, name + ".mp3")
        subprocess.run(
            [ff, "-y", "-loglevel", "error", "-i", wav_path,
             "-codec:a", "libmp3lame", "-b:a", MP3_KBPS, "-ac", "1", mp3_path],
            check=True,
        )
        os.remove(wav_path)
        path = mp3_path

    print("  %-24s %5.2fs  %6.1f KB" % (os.path.basename(path), len(x) / SR,
                                        os.path.getsize(path) / 1024))


# ── آلات ─────────────────────────────────────────────────────────────

def brass(freq, dur, partials=11, bright=1.0, detune=0.006):
    """نفخ لامع — أساس أصوات البرامج."""
    t = tline(dur)
    out = np.zeros(len(t))
    vib = 1.0 + 0.004 * np.sin(2 * np.pi * 5.5 * t)
    for k in range(1, partials + 1):
        amp = (1.0 / k ** 1.15) * (bright if k > 3 else 1.0)
        out += amp * np.sin(2 * np.pi * freq * k * t * vib)
        out += amp * 0.5 * np.sin(2 * np.pi * freq * (1 + detune) * k * t)
    out *= perc(len(t), 0.012, dur * 0.55, curve=2.2)
    return lowpass_fast(out, 4200) * 0.4


def bell(freq, dur, seed=0):
    """جرس — شراكات غير متناغمة، بتلمع وبتضل رنّة."""
    t = tline(dur)
    out = np.zeros(len(t))
    for mult, amp, dec in ((1.0, 1.0, 1.0), (2.76, 0.6, 0.7), (5.40, 0.35, 0.45), (8.93, 0.2, 0.3)):
        out += amp * np.sin(2 * np.pi * freq * mult * t) * perc(len(t), 0.003, dur * dec, 3.2)
    return out * 0.32


def marimba(freq, dur):
    t = tline(dur)
    out = np.sin(2 * np.pi * freq * t)
    out += 0.35 * np.sin(2 * np.pi * freq * 4 * t)
    out += 0.12 * np.sin(2 * np.pi * freq * 10 * t)
    return out * perc(len(t), 0.004, dur * 0.35, 4.5) * 0.55


def boom(dur=0.55, f0=130, f1=42):
    """ضربة عميقة — بتنحسّ بالصدر."""
    s = chirp(f0, f1, dur, curve=0.45)
    return s * perc(n(dur), 0.004, dur * 0.4, 3.0) * 0.95


def cymbal(dur, tone=7000, seed=1):
    s = highpass_fast(noise(dur, seed), tone)
    return s * perc(n(dur), 0.002, dur * 0.4, 2.6) * 0.5


def whoosh(dur, seed=2, up=True, tone=1500):
    """كنسة هوائية للانتقالات."""
    s = noise(dur, seed)
    t = np.linspace(0, 1, len(s))
    shape = t if up else (1 - t)
    s = highpass_fast(s, 220)
    s *= (0.25 + shape) ** 2
    s = lowpass_fast(s, tone + 5200 * (shape.mean() + 0.4))
    s *= np.sin(np.pi * t) ** 1.3
    return s * 0.7


def chord(freqs, dur, inst=brass, spread=0.0):
    out = buf(dur)
    for i, f in enumerate(freqs):
        place(out, inst(f, dur - i * spread), i * spread)
    return out


# ── السلّم ───────────────────────────────────────────────────────────

C4, E4, G4 = 261.63, 329.63, 392.00
C5, D5, E5, F5, G5, A5 = 523.25, 587.33, 659.25, 698.46, 783.99, 880.00
C6, E6, G6 = 1046.50, 1318.51, 1567.98
MAJOR = [C4, E4, G4, C5, E5]


# ── الأصوات ──────────────────────────────────────────────────────────

def sfx_intro():
    """المقدمة (٤ ثواني) — FLASH ٠، CLASH ٠٫٥٥، BUILD ١٫٤٠، WIN ٢٫٨٥."""
    out = buf(4.0)
    # وميض البداية
    place(out, cymbal(0.9, 6000, seed=3) * 0.8, 0.0)
    place(out, whoosh(0.55, seed=4, up=True) * 0.8, 0.0)
    # التصادم — لوحان بيتخبطوا
    place(out, boom(0.8, 150, 40) * 1.1, CLASH := 0.55)
    place(out, chord([C4, G4, C5], 0.7) * 0.9, CLASH)
    place(out, cymbal(0.7, 4500, seed=5), CLASH)
    # البناء — سلّم صاعد بينتهي عند لحظة الفوز
    for i, f in enumerate((C5, E5, G5)):
        place(out, brass(f, 0.34) * 0.7, 1.40 + i * 0.22)
    place(out, whoosh(1.0, seed=6, up=True) * 0.55, 1.85)
    # الفوز — وتر كامل ولمعة ذهبية
    place(out, chord([C5, E5, G5, C6], 1.15, spread=0.012) * 1.0, WIN := 2.85)
    place(out, boom(0.9, 120, 38), WIN)
    place(out, bell(C6, 1.1, seed=7) * 0.8, WIN + 0.04)
    place(out, cymbal(1.1, 5200, seed=8) * 0.7, WIN)
    return out


def sfx_round_start():
    """بداية الجولة (١٫٧ث) — القرص بينط، الاسم بيهبط بضربة، واللافتة بتكنس."""
    out = buf(1.7)
    place(out, whoosh(0.42, seed=9, up=True) * 0.7, 0.02)   # القرص داخل
    place(out, marimba(G5, 0.2) * 0.5, 0.06)
    place(out, boom(0.6, 140, 45) * 1.05, 0.30)             # ضربة الاسم
    place(out, chord([C5, G5], 0.55) * 0.85, 0.30)
    place(out, cymbal(0.45, 6500, seed=10) * 0.6, 0.30)
    place(out, whoosh(0.5, seed=11, up=False) * 0.8, 0.50)  # كنس اللافتة
    place(out, marimba(C6, 0.1) * 0.4, 0.50)
    place(out, bell(G5, 0.7, seed=12) * 0.8, 0.52)
    return out


def sfx_versus():
    """«استعدوا» (١٫٦ث) — كرتان بيدخلوا، رجّة ٠٫٤٤، وشارة «ضد» ٠٫٥."""
    out = buf(1.6)
    place(out, whoosh(0.46, seed=13, up=True) * 0.85, 0.06)   # الكرت الأول
    place(out, whoosh(0.46, seed=14, up=True) * 0.85, 0.20)   # الكرت التاني
    place(out, boom(0.7, 160, 38) * 1.15, 0.44)               # الرجّة
    place(out, softclip(noise(0.12, seed=15) * perc(n(0.12), 0.001, 0.05, 5.0), 3.0) * 0.5, 0.44)
    place(out, chord([C4, E4, G4], 0.8, spread=0.0) * 0.7, 0.50)  # شارة «ضد»
    place(out, cymbal(0.8, 3800, seed=16) * 0.75, 0.50)
    place(out, bell(C5, 0.9, seed=17) * 0.45, 0.52)
    return out


def sfx_count():
    """عدّ النقاط (٠٫٩ث) — دقّات بتتسارع وبتقفل بنغمة."""
    out = buf(0.95)
    at, step = 0.0, 0.055
    while at < 0.78:
        place(out, marimba(G5, 0.07) * 0.42, at)
        step = max(0.026, step * 0.93)
        at += step
    place(out, bell(C6, 0.35, seed=18) * 0.7, 0.80)
    return out


def sfx_crown():
    """التتويج واللافتة الذهبية (١٫٣ث) — كنسة هوائية وسلّم أجراس."""
    out = buf(1.3)
    place(out, whoosh(0.7, seed=19, up=True) * 0.5, 0.0)
    for i, f in enumerate((C5, E5, G5, C6)):
        place(out, bell(f, 0.85 - i * 0.08, seed=20 + i) * (0.75 - i * 0.06), 0.06 + i * 0.075)
    place(out, cymbal(0.9, 7200, seed=25) * 0.45, 0.30)
    place(out, chord([C5, G5, E6], 0.7) * 0.4, 0.36)
    return out


def sfx_fanfare():
    """النتيجة النهائية (٢٫٦ث) — لحن نفخ وبعده وتر كبير."""
    out = buf(2.6)
    motif = ((G4, 0.0, 0.20), (C5, 0.19, 0.20), (E5, 0.38, 0.20), (G5, 0.57, 0.34))
    for f, at, dur in motif:
        place(out, brass(f, dur + 0.12) * 0.95, at)
        place(out, marimba(f * 2, 0.12) * 0.25, at)
    place(out, boom(0.5, 120, 46) * 0.7, 0.0)
    place(out, cymbal(0.5, 6800, seed=26) * 0.5, 0.57)
    # الوتر الكبير
    place(out, chord([C4, G4, C5, E5, G5], 1.6, spread=0.018) * 1.0, 0.95)
    place(out, boom(1.2, 150, 36) * 1.1, 0.95)
    place(out, cymbal(1.5, 4200, seed=27) * 0.7, 0.95)
    place(out, bell(C6, 1.4, seed=28) * 0.55, 1.0)
    place(out, bell(G6, 1.2, seed=29) * 0.35, 1.1)
    return comb(out, 0.085, 0.22, repeats=3)


def sfx_firework():
    """طقّة ألعاب نارية (٠٫٨ث) — صفّارة صاعدة وبعدها فرقعة وطرطقة."""
    out = buf(0.8)
    place(out, chirp(900, 2600, 0.22, curve=1.6) * perc(n(0.22), 0.02, 0.12, 2.0) * 0.28, 0.0)
    place(out, softclip(noise(0.2, seed=30) * perc(n(0.2), 0.001, 0.07, 4.0), 2.5) * 0.85, 0.22)
    place(out, boom(0.3, 90, 40) * 0.5, 0.22)
    crackle = noise(0.5, seed=31)
    rng = np.random.default_rng(32)
    crackle *= (rng.random(len(crackle)) > 0.955)     # طرطقة متفرّقة
    place(out, highpass_fast(crackle, 2400) * np.linspace(1, 0, len(crackle)) ** 2 * 0.6, 0.26)
    return out


def sfx_join():
    """لاعب انضم (٠٫٤٥ث) — نغمتان صاعدتان، ودّية ومش مزعجة."""
    out = buf(0.45)
    place(out, marimba(G5, 0.18) * 0.8, 0.0)
    place(out, marimba(C6, 0.3) * 0.85, 0.08)
    place(out, bell(C6, 0.32, seed=33) * 0.3, 0.08)
    return out


def sfx_tap():
    """لمسة زر (٠٫١ث) — قصيرة وجافة."""
    out = buf(0.1)
    click = noise(0.03, seed=34) * perc(n(0.03), 0.0005, 0.008, 6.0)
    place(out, highpass_fast(click, 1800) * 0.6, 0.0)
    place(out, marimba(A5, 0.08) * 0.45, 0.0)
    return out


SOUNDS = {
    "sfx_intro": sfx_intro,
    "sfx_round_start": sfx_round_start,
    "sfx_versus": sfx_versus,
    "sfx_count": sfx_count,
    "sfx_crown": sfx_crown,
    "sfx_fanfare": sfx_fanfare,
    "sfx_firework": sfx_firework,
    "sfx_join": sfx_join,
    "sfx_tap": sfx_tap,
}


def main():
    os.makedirs(OUT, exist_ok=True)
    ff = ffmpeg_exe()
    if not ff:
        print("تنبيه: ما في ffmpeg — رح تنحفظ WAV بدل MP3.")
    print("توليد الأصوات في %s" % OUT)
    for name, fn in SOUNDS.items():
        save(name, fn(), ff)


if __name__ == "__main__":
    main()
