import pandas as pd
import os
from glob import glob
import csv
import math
from statistics import median
from statistics import stdev
from scipy.stats import kurtosis,skew

def feature(FOLDER):
    df_list = []
    sum_df = pd.DataFrame()
    PATH = "./"
    OUTPUT_PATH = './OUTPUT1/'
    FILE_PATH = PATH + FOLDER
    OUTPUT_FILE_PATH = OUTPUT_PATH + FOLDER + '.csv'
    count = 0
    final = []
    for file in os.listdir(FILE_PATH):
        df = pd.read_csv(os.path.join(FILE_PATH,file))
        df = df[(df['label'] == FOLDER).idxmax():]
        df = df.reset_index(drop=True)
        df["acc_x"]= df["acc_x"].astype('float64')
        df["acc_y"]= df["acc_y"].astype('float64')
        df["acc_z"]= df["acc_z"].astype('float64')
        df['mag'] = df['acc_x']*df['acc_x'] + df['acc_y']*df['acc_y'] + df['acc_z']*df['acc_z']
        OUTPUT_FILE_PATH = OUTPUT_PATH + FOLDER + '/' + file
        OUTPUT_FOLDER_PATH = OUTPUT_PATH + FOLDER
        if not os.path.exists(OUTPUT_FOLDER_PATH):
            os.makedirs(OUTPUT_FOLDER_PATH)
        exists = os.path.isfile(OUTPUT_FILE_PATH)
        if(exists):
            print(OUTPUT_FILE_PATH + " exist , skip...")
        else:
            df.to_csv(OUTPUT_FILE_PATH,index=False)
        X = []
        Y = []
        Z = []
        MAG = []
        ymag = []
        df_count = df.shape[0]
        FALL_SIZE = df_count
        for i in range(0,FALL_SIZE):
            X.append(df.iloc[i, 2])
            Y.append(df.iloc[i, 3])
            Z.append(df.iloc[i, 4])
            MAG.append(df.iloc[i, 12])
            ymag.append(float(Y[i])/float(math.sqrt(MAG[i])))
        TA = [math.asin(ymag[k]) for k in range(0,FALL_SIZE)]
        avgX = sum(X)/len(X)
        avgY = sum(Y)/len(Y)
        avgZ = sum(Z)/len(Z)
        medianX = median(X)
        medianY = median(Y)
        medianZ = median(Z)
        stdX = stdev(X)
        stdY = stdev(Y)
        stdZ = stdev(Z)
        skewX = skew(X)
        skewY = skew(Y)
        skewZ = skew(Z)
        kurtosisX = kurtosis(X)
        kurtosisY = kurtosis(Y)
        kurtosisZ = kurtosis(Z)
        minX = min(X)
        minY = min(Y)
        minZ = min(Z)
        maxX = max(X)
        maxY = max(Y)
        maxZ  = max(Z)
        slope = math.sqrt((maxX - minX)**2 + (maxY - minY)**2 + (maxZ - minZ)**2)
        meanTA = sum(TA)/len(TA)
        stdTA = stdev(TA)
        skewTA = skew(TA)
        kurtosisTA = kurtosis(TA)
        absX = sum([abs(X[k] - avgX) for k in range(0,FALL_SIZE) ]) / len(X)
        absY = sum([abs(Y[k] - avgY) for k in range(0,FALL_SIZE) ]) / len(Y)
        absZ = sum([abs(Z[k] - avgZ) for k in range(0,FALL_SIZE) ]) / len(Z)
        abs_meanX = sum([abs(X[k]) for k in range(0,FALL_SIZE)])/len(X)
        abs_meanY = sum([abs(Y[k]) for k in range(0,FALL_SIZE)])/len(Y)
        abs_meanZ = sum([abs(Z[k]) for k in range(0,FALL_SIZE)])/len(Z)
        abs_medianX = median([abs(X[k]) for k in range(0,FALL_SIZE)])
        abs_medianY = median([abs(Y[k]) for k in range(0,FALL_SIZE)])
        abs_medianZ = median([abs(Z[k]) for k in range(0,FALL_SIZE)])
        abs_stdX = stdev([abs(X[k]) for k in range(0,FALL_SIZE)])
        abs_stdY = stdev([abs(Y[k]) for k in range(0,FALL_SIZE)])
        abs_stdZ = stdev([abs(Z[k]) for k in range(0,FALL_SIZE)])
        abs_skewX = skew([abs(X[k]) for k in range(0,FALL_SIZE)])
        abs_skewY = skew([abs(Y[k]) for k in range(0,FALL_SIZE)])
        abs_skewZ = skew([abs(Z[k]) for k in range(0,FALL_SIZE)])
        abs_kurtosisX = kurtosis([abs(X[k]) for k in range(0,FALL_SIZE)])
        abs_kurtosisY = kurtosis([abs(Y[k]) for k in range(0,FALL_SIZE)])
        abs_kurtosisZ = kurtosis([abs(Z[k]) for k in range(0,FALL_SIZE)])
        abs_minX = min([abs(X[k]) for k in range(0,FALL_SIZE)])
        abs_minY = min([abs(Y[k]) for k in range(0,FALL_SIZE)])
        abs_minZ = min([abs(Z[k]) for k in range(0,FALL_SIZE)])
        abs_maxX = max([abs(X[k]) for k in range(0,FALL_SIZE)])
        abs_maxY = max([abs(Y[k]) for k in range(0,FALL_SIZE)])
        abs_maxZ  = max([abs(Z[k]) for k in range(0,FALL_SIZE)])
        abs_slope = math.sqrt((abs_maxX - abs_minX)**2 + (abs_maxY - abs_minY)**2 + (abs_maxZ - abs_minZ)**2)
        meanMag = sum(MAG)/len(MAG)
        stdMag = stdev(MAG)
        minMag = min(MAG)
        maxMag = max(MAG)
        DiffMinMaxMag = maxMag - minMag
        ZCR_Mag = 0
        AvgResAcc = (1/len(MAG))*sum(MAG)
        test = [avgX,avgY,avgZ,medianX,medianY,medianZ,stdX,stdY,stdZ,skewX,skewY,skewZ,kurtosisX,kurtosisY,kurtosisZ,
                                          minX,minY,minZ,maxX,maxY,maxZ,slope,meanTA,stdTA,skewTA,kurtosisTA,absX,
                                          absY,absZ,abs_meanX,abs_meanY,abs_meanZ,abs_medianX,abs_medianY,abs_medianZ,
                                          abs_stdX,abs_stdY,abs_stdZ,abs_skewX,abs_skewY,abs_skewZ,abs_kurtosisX,
                                          abs_kurtosisY,abs_kurtosisZ,abs_minX,abs_minY,abs_minZ,abs_maxX,abs_maxY
                                          ,abs_maxZ,abs_slope,meanMag,stdMag,minMag,maxMag,DiffMinMaxMag,ZCR_Mag,AvgResAcc,FOLDER]
        final.append(test)
    return final

OUTPUT_PATH = './OUTPUT/'
#folders=['BSC', 'CHU']
folders=['BSC', 'CHU','CSI','CSO', 'FKL','FOL','JOG','JUM','SBE','SBW','SCH','SDL','SIT','SLH','SLW','SRH','STD','STN','STU','WAL']
for FOLDER in folders:
    OUTPUT_FILE_PATH = OUTPUT_PATH + FOLDER + '.csv'
    if(os.path.isfile(OUTPUT_FILE_PATH)):
        os.remove(OUTPUT_FILE_PATH)            
    with open(OUTPUT_FILE_PATH,'a') as f1:
        writer=csv.writer(f1, delimiter=',',lineterminator='\n',)
        writer.writerow(['AvgX','AvgY','AvgZ','MedianX','MedianY','MedianZ','StdX',
        'StdY','StdZ','SkewX','SkewY','SkewZ','KurtosisX','KurtosisY','KurtosisZ','MinX','MinY',
        'MinZ','MaxX','MaxY','MaxZ','Slope','MeanTA','StdTA','SkewTA','KurtosisTA',
        'AbsX','AbsY','AbsZ','AbsMeanX','AbsMeanY','AbsMeanZ','AbsMedianX','AbsMedianY','AbsMedianZ',
        'AbsStdX','AbsStdY','AbsStdZ','AbsSkewX','AbsSkewY','AbsSkewZ',
        'AbsKurtosisX','AbsKurtosisY','AbsKurtosisZ','AbsMinX','AbsMinY','AbsMinZ',
        'AbsMaxX','AbsMaxY','AbsMaxZ','AbsSlope','MeanMag',
        'StdMag','MinMag','MaxMag','DiffMinMaxMag','ZCR_Mag','AverageResultantAcceleration','label'])
        feature_data = feature(FOLDER)
        data_len = len(feature_data)
        for p in range(0,data_len):
            writer.writerow(feature_data[p])
        print(FOLDER," - ", data_len," records")
		
# get all data
fs = os.listdir(OUTPUT_PATH)
all_data = pd.DataFrame()
for f in fs:    
    file_path = os.path.join(OUTPUT_PATH, f)
    print(file_path)
    data = pd.read_csv(file_path, index_col=False,engine='python')
    data = data.iloc[0:,0:59]
    all_data = all_data.append(data)
#print(all_data)   
all_data.to_csv( "all_data1all.csv", index=False, encoding='utf-8-sig')

