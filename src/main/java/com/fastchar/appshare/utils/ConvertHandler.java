package com.fastchar.appshare.utils;

import com.jcraft.jzlib.ZStream;

import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.CRC32;

import javax.imageio.ImageIO;

public class ConvertHandler {
    private static final String PNG_TYPE = "png";

    private static final int SMALL_ICON_WIDTH = 57;

    protected List<PNGTrunk> trunks = null;

    public ConvertHandler() {

    }

    protected PNGTrunk getTrunk(String szName) {
        if (this.trunks == null) {
            return null;
        }
        PNGTrunk ret = null;
        for (int n = 0; n < this.trunks.size(); n++) {
            PNGTrunk trunk = (PNGTrunk) this.trunks.get(n);
            if (trunk.getName().equalsIgnoreCase(szName)) {
                ret = trunk;
                break;
            }
        }
        return ret;
    }


    public void convertPNGFile(File pngFile, File newPngFile, File upNewFile, File downNewFile) {
        try {
            DataInputStream file = new DataInputStream(new FileInputStream(pngFile));
            byte[] nPNGHeader = new byte[8];
            file.read(nPNGHeader);

            this.trunks = new ArrayList<PNGTrunk>();
            int nDats = 0;

            if ((nPNGHeader[0] == -119) && (nPNGHeader[1] == 80)
                    && (nPNGHeader[2] == 78) && (nPNGHeader[3] == 71)
                    && (nPNGHeader[4] == 13) && (nPNGHeader[5] == 10)
                    && (nPNGHeader[6] == 26) && (nPNGHeader[7] == 10)) {
                PNGTrunk trunk;
                do {
                    trunk = PNGTrunk.generateTrunk(file);
                    this.trunks.add(trunk);
                    if (trunk.getName().equalsIgnoreCase("IDAT")) {
                        nDats++;
                    }
                } while (!trunk.getName().equalsIgnoreCase("IEND"));
            }
            file.close();

            if (getTrunk("CgBI") != null) {
                PNGIHDRTrunk ihdrTrunk = (PNGIHDRTrunk) getTrunk("IHDR");

                boolean isBaseType = ihdrTrunk.m_nWidth == SMALL_ICON_WIDTH;
                decodePNGData(trunks, ihdrTrunk.m_nWidth, ihdrTrunk.m_nHeight);

                if (isBaseType || nDats == 1) {
                    genNewPNGFile(newPngFile, 1);
                } else {
                    genNewPNGFile(upNewFile, 1);
                    genNewPNGFile(downNewFile, 2);

                    combinePNG(newPngFile, upNewFile, downNewFile);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void decodePNGData(List<PNGTrunk> dataTrunkList, int width, int height) {
        int nMaxInflateBuffer = (4 * width + 1) * height;

        for (PNGTrunk dataTrunk : dataTrunkList) {
            if (!dataTrunk.getName().equalsIgnoreCase("IDAT")) {
                continue;
            }

            byte[] outputBuffer = new byte[nMaxInflateBuffer];

            ZStream inStream = new ZStream();
            inStream.avail_in = dataTrunk.getSize();
            inStream.next_in_index = 0;
            inStream.next_in = dataTrunk.getData();
            inStream.next_out_index = 0;
            inStream.next_out = outputBuffer;
            inStream.avail_out = outputBuffer.length;

            if (inStream.inflateInit(-15) != 0) {
                return;
            }

            int nResult = inStream.inflate(0);
            switch (nResult) {
                case 2:
                    nResult = -3;
                case -4:
                case -3:
                    inStream.inflateEnd();
                    return;
            }

            nResult = inStream.inflateEnd();

            if (inStream.total_out > nMaxInflateBuffer) {
            }

            int nIndex = 0;

            for (int y = 0; y < height; y++) {
                nIndex++;
                for (int x = 0; x < width; x++) {
                    byte nTemp = outputBuffer[nIndex];
                    outputBuffer[nIndex] = outputBuffer[(nIndex + 2)];
                    outputBuffer[(nIndex + 2)] = nTemp;
                    nIndex += 4;
                }
            }

            ZStream deStream = new ZStream();
            int nMaxDeflateBuffer = nMaxInflateBuffer + 1024;
            byte[] deBuffer = new byte[nMaxDeflateBuffer];

            deStream.avail_in = outputBuffer.length;
            deStream.next_in_index = 0;
            deStream.next_in = outputBuffer;
            deStream.next_out_index = 0;
            deStream.next_out = deBuffer;
            deStream.avail_out = deBuffer.length;
            deStream.deflateInit(9);
            nResult = deStream.deflate(4);

            if (deStream.total_out > nMaxDeflateBuffer) {
            }
            byte[] newDeBuffer = new byte[(int) deStream.total_out];
            for (int n = 0; n < deStream.total_out; n++) {
                newDeBuffer[n] = deBuffer[n];
            }
            CRC32 crc32 = new CRC32();
            crc32.update(dataTrunk.getName().getBytes());
            crc32.update(newDeBuffer);
            long lCRCValue = crc32.getValue();

            dataTrunk.m_nData = newDeBuffer;
            dataTrunk.m_nCRC[0] = (byte) (int) ((lCRCValue & 0xFF000000) >> 24);
            dataTrunk.m_nCRC[1] = (byte) (int) ((lCRCValue & 0xFF0000) >> 16);
            dataTrunk.m_nCRC[2] = (byte) (int) ((lCRCValue & 0xFF00) >> 8);
            dataTrunk.m_nCRC[3] = (byte) (int) (lCRCValue & 0xFF);
            dataTrunk.m_nSize = newDeBuffer.length;
        }
    }

    private void genNewPNGFile(File newFile, int order) throws FileNotFoundException, IOException {
        FileOutputStream outStream = new FileOutputStream(newFile);
        byte[] pngHeader = {-119, 80, 78, 71, 13, 10, 26, 10};
        outStream.write(pngHeader);
        int i = 1;
        for (int n = 0; n < this.trunks.size(); n++) {
            PNGTrunk trunk = (PNGTrunk) this.trunks.get(n);
            if (trunk.getName().equalsIgnoreCase("CgBI")) {
                continue;
            }
            if (trunk.getName().equalsIgnoreCase("IDAT")) {
                if (i != order) {
                    i++;
                    continue;
                }
                i++;
            }
            trunk.writeToStream(outStream);
        }
        outStream.close();
    }

    private void combinePNG(File outFile, File upPNGFile, File downPNGFile) {
        try {
            BufferedImage imageFirst = ImageIO.read(upPNGFile);
            int width = imageFirst.getWidth();
            int height = imageFirst.getHeight();
            int[] imageArrayFirst = new int[width * height];
            imageArrayFirst = imageFirst.getRGB(0, 0, width, height / 2, imageArrayFirst, 0, width);

            BufferedImage imageSecond = ImageIO.read(downPNGFile);
            int[] imageArraySecond = new int[width * height];
            imageArraySecond = imageSecond.getRGB(0, 0, width, height / 2, imageArraySecond, 0, width);

            BufferedImage imageResult = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
            imageResult.setRGB(0, 0, width, height / 2, imageArrayFirst, 0, width);
            imageResult.setRGB(0, height / 2, width, height / 2, imageArraySecond, 0, width);
            ImageIO.write(imageResult, PNG_TYPE, outFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}