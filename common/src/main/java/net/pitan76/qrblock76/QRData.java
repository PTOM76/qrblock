package net.pitan76.qrblock76;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.util.HashMap;
import java.util.Map;

public class QRData {
    private static final Map<String, QRData> cacheMap = new HashMap<>();

    public final BitMatrix matrix;
    public final int size;
    public final float scale;

    private static final int[] SIZE_THRESHOLDS = {10, 20, 35, 50};
    private static final int[] QR_SIZES = {21, 25, 29, 33, 37};

    public static int getOptimalQRSize(String data) {
        int dataLength = data.length();
        for (int i = 0; i < SIZE_THRESHOLDS.length; i++)
            if (dataLength <= SIZE_THRESHOLDS[i])
                return QR_SIZES[i];

        return QR_SIZES[QR_SIZES.length - 1];
    }

    public QRData(BitMatrix matrix, int size) {
        this(matrix, size, 1.0f / matrix.getWidth());
    }

    public QRData(BitMatrix matrix, int size, float scale) {
        this.matrix = matrix;
        this.size = size;
        this.scale = scale;
    }

    public static QRData getOrCreateQRData(String data) {
        QRData qrdata = cacheMap.get(data);
        if (qrdata == null) {
            int size = getOptimalQRSize(data);
            try {
                BitMatrix matrix = new QRCodeWriter().encode(data, BarcodeFormat.QR_CODE, size, size);
                qrdata = new QRData(matrix, size);
                cacheMap.put(data, qrdata);
            } catch (WriterException ex) {
                return null;
            }
        }
        return qrdata;
    }

}
