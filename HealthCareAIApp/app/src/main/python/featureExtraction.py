import librosa as lb
import numpy as np
from scipy.signal import butter, lfilter


# Sample rate and desired cutoff frequencies (in Hz).
fs = 4000.0
lowcut = 100.0
highcut = 1800.0

#Set maxpad length as 79 <--(Sampling rate*5s)/256(hop length)
def build_feat(fpath):
    max_pad_len = 79
    wav,  rate = lb.load(fpath, sr=4000)
    bb = butter_bandpass_filter(wav, lowcut, highcut, fs, order=12)
    bb = bb.astype('float32')
    #limit the length of samples to only 6s (6*4000)
    if bb.shape[0] > 20000:
        bb = bb[0:20000]
    X_sample = lb.feature.mfcc(bb, sr=rate, n_fft=512,  win_length=400, n_mfcc=20, hop_length = 256, n_mels = 128, fmin = 100, fmax = 1800)
    pad_width = max_pad_len - X_sample.shape[1]
    X_sample = np.pad(X_sample, pad_width=((0, 0), (0, pad_width)), mode='constant')
    X = X_sample.T.copy()
    inp = np.expand_dims(X, axis=0)

    return inp


def butter_bandpass(lowcut, highcut, fs, order=5):
    nyq = 0.5 * fs
    low = lowcut / nyq
    high = highcut / nyq
    b, a = butter(order, [low, high], btype='band')
    return b, a

def butter_bandpass_filter(data, lowcut, highcut, fs, order=5):
    b, a = butter_bandpass(lowcut, highcut, fs, order=order)
    y = lfilter(b, a, data)
    return y