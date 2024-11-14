package com.app.smartpos.devices.urovo;

import android.content.Context;
import android.device.PrinterManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import com.google.zxing.BarcodeFormat;
import com.newland.ndk.Print;
import com.urovo.file.logfile;
import com.urovo.sdk.print.EncodingHandler;
import com.urovo.sdk.print.QRCodeUtil;
import com.urovo.sdk.print.TypefaceHelper;
import com.urovo.sdk.utils.FilesUtil;
import java.io.File;

public class CustomPrinterProviderImpl {
    public static final String TAG = com.app.smartpos.devices.urovo.PrinterProviderImpl.class.getSimpleName();
    private boolean initPage = false;
    private Context mContext;
    private PrinterManager mPrinter;
    private int feedLine = 0;
    int currentYPoint = 0;
    public static final int MAX_PAGEWIDTH = 384;
    public static final int DEF_FONT_SIZE_SMALL = 16;
    public static final int DEF_FONT_SIZE = 24;
    public static final int DEF_FONT_SIZE_BIG = 32;
    private static final int MEG_CMD_STARTPRINT = 1;
    private static final int MEG_CMD_STARTPRINT_CACHE = 2;
    private int mFontsize = 24;
    private boolean enablePrintY = false;
    private int mStatus = 0;
    public static String fontName_default = "simsun";
    private String mFontNameLast = "";
    private Typeface mTypefaceLast = null;
    private Paint mPaint = null;
    public static PrinterProviderImpl mPrinterProvider;

    public static PrinterProviderImpl getInstance(Context context) {
        if (mPrinterProvider == null) {
            mPrinterProvider = new PrinterProviderImpl(context);
        }

        return mPrinterProvider;
    }

    public CustomPrinterProviderImpl(Context context) {
        this.mContext = context;
    }

    public void initPrint() {
        logfile.printLog(TAG + "===initPrint");
        if (this.mPrinter == null) {
            this.mPrinter = new PrinterManager();
        }

        this.mPrinter.open();
        this.mPrinter.setupPage(384, -1);
        this.currentYPoint = 0;
        this.initPage = true;
        this.enablePrintY = false;
        if (this.mPaint == null) {
            this.mPaint = new Paint();
            this.mPaint.reset();
            this.mPaint.setFlags(Paint.FILTER_BITMAP_FLAG);
            this.mPaint.setColor(-16777216);
            this.mPaint.setUnderlineText(false);
            this.mPaint.setStrikeThruText(false);
        }

    }

    public int close() {
        logfile.printLog(TAG + "===close");
        if (this.mPrinter != null) {
            this.mPrinter.setupPage(-1, -1);
            this.mPrinter.clearPage();
            this.mPrinter.close();
            this.mPrinter = null;
            this.mStatus = 0;
        }

        this.currentYPoint = 0;
        this.initPage = false;
        this.mPaint = null;
        this.enablePrintY = false;
        return 0;
    }

    public void enablePrintY(boolean enable) {
        this.enablePrintY = enable;
    }

    public int getStatus() {
        logfile.printLog(TAG + "===getStatus");
        int ret = -1;
        if (this.mPrinter == null) {
            logfile.printLog(TAG + "===mPrinter is NULL");
            return ret;
        } else {
            if (this.mStatus != 247) {
                ret = this.mPrinter.getStatus();
                if (ret == 0) {
                    this.mStatus = 0;
                } else if (ret == -1) {
                    this.mStatus = 240;
                } else if (ret == -2) {
                    this.mStatus = 243;
                } else if (ret == -3) {
                    this.mStatus = 225;
                } else if (ret == -4) {
                    this.mStatus = 247;
                } else if (ret == -256) {
                    this.mStatus = 251;
                } else if (ret == -257) {
                    this.mStatus = 242;
                } else {
                    this.mStatus = ret;
                }
            }

            return this.mStatus;
        }
    }

    public void addText(Bundle format, String text) {
        logfile.getBundleStringLogOut(TAG + "===addText", format);
        logfile.printLog(TAG + "===addText, text:" + text);
        if (this.mPrinter == null) {
            logfile.printLog(TAG + "===mPrinter is NULL");
        } else if (!TextUtils.isEmpty(text)) {
            int fontStyle = 0;
            int nowrap = 1;
            int xPoint = 0;
            int fontsize = 1;
            int font = 1;
            int lineHeight = 5;
            int align = 0;
            boolean fontBold = false;
            boolean newline = true;
            String fontName = "";
            if (!this.initPage) {
                this.mPrinter.setupPage(384, -1);
                this.initPage = true;
            }

            if (this.currentYPoint == 0) {
                this.currentYPoint += 5;
            }

            if (format != null) {
                font = format.getInt("font", 1);
                align = format.getInt("align", 0);
                fontName = format.getString("fontName", fontName_default);
                fontBold = format.getBoolean("fontBold", false);
                newline = format.getBoolean("newline", true);
                lineHeight = format.getInt("lineHeight", 5);
            }

            fontName = this.checkFontFileExist(fontName);
            if (font == 0) {
                fontsize = 16;
            } else if (font == 1) {
                fontsize = 24;
            } else if (font == 2) {
                fontsize = 32;
            } else {
                fontsize = 24;
            }

            if (format != null && format.containsKey("fontSize")) {
                int fontsize2 = format.getInt("fontSize", 0);
                if (fontsize2 > 0) {
                    fontsize = fontsize2;
                }
            }

            if (fontBold) {
                fontStyle = 1;
            }

            int maxLine = 384;
            Paint mPaint = this.getPaintCache(fontsize, fontBold, false, fontName);
            String[] textArr = text.split("\n");
            int i;
            StringBuffer stringBuffer;
            int a;
            float width;
            float nexWidth;
            String lastStr;
            if (!newline) {
                for(i = 0; i < textArr.length; ++i) {
                    text = textArr[i];
                    stringBuffer = new StringBuffer();

                    for(a = 0; a < text.length(); ++a) {
                        stringBuffer.append(text.charAt(a));
                        width = mPaint.measureText(stringBuffer.toString());
                        nexWidth = 0.0F;
                        if (a < text.length() - 1) {
                            lastStr = String.valueOf(text.charAt(a + 1));
                            if (!TextUtils.isEmpty(lastStr)) {
                                nexWidth = mPaint.measureText(lastStr);
                            }
                        }

                        if (width + nexWidth > (float)maxLine) {
                            if (this.enablePrintY) {
                                logfile.printLog(TAG + "===currentYPoint=" + this.currentYPoint);
                            }

                            this.currentYPoint += this.mPrinter.drawTextEx(stringBuffer.toString(), 0, this.currentYPoint, 384, -1, fontName, fontsize, 0, fontStyle, nowrap) + lineHeight;
                            stringBuffer.setLength(0);
                        } else if (a == text.length() - 1) {
                            lastStr = stringBuffer.toString();
                            if (TextUtils.isEmpty(lastStr)) {
                                return;
                            }

                            if (1 == align) {
                                xPoint = (int)((float)maxLine - width) / 2;
                            } else if (2 == align) {
                                xPoint = (int)((float)maxLine - width);
                            }

                            if (this.enablePrintY) {
                                logfile.printLog(TAG + "===currentYPoint=" + this.currentYPoint);
                            }

                            int var22 = this.mPrinter.drawTextEx(stringBuffer.toString(), xPoint, this.currentYPoint, 384, -1, fontName, fontsize, 0, fontStyle, nowrap) + lineHeight;
                        }
                    }
                }
            } else {
                for(i = 0; i < textArr.length; ++i) {
                    text = textArr[i];
                    stringBuffer = new StringBuffer();

                    for(a = 0; a < text.length(); ++a) {
                        stringBuffer.append(text.charAt(a));
                        width = mPaint.measureText(stringBuffer.toString());
                        nexWidth = 0.0F;
                        if (a < text.length() - 1) {
                            lastStr = String.valueOf(text.charAt(a + 1));
                            if (!TextUtils.isEmpty(lastStr)) {
                                nexWidth = mPaint.measureText(lastStr);
                            }
                        }

                        if (width + nexWidth > (float)maxLine) {
                            if (this.enablePrintY) {
                                logfile.printLog(TAG + "===currentYPoint=" + this.currentYPoint);
                            }

                            this.currentYPoint += this.mPrinter.drawTextEx(stringBuffer.toString(), 0, this.currentYPoint, 384, -1, fontName, fontsize, 0, fontStyle, nowrap) + lineHeight;
                            stringBuffer.setLength(0);
                        } else if (a == text.length() - 1) {
                            lastStr = stringBuffer.toString();
                            if (TextUtils.isEmpty(lastStr)) {
                                return;
                            }

                            if (1 == align) {
                                xPoint = (int)((float)maxLine - width) / 2;
                            } else if (2 == align) {
                                xPoint = (int)((float)maxLine - width);
                            }

                            if (this.enablePrintY) {
                                logfile.printLog(TAG + "===currentYPoint=" + this.currentYPoint);
                            }

                            this.currentYPoint += this.mPrinter.drawTextEx(stringBuffer.toString(), xPoint, this.currentYPoint, 384, -1, fontName, fontsize, 0, fontStyle, nowrap) + lineHeight;
                        }
                    }
                }
            }

        }
    }

    public void addTextOnlyLeft(Bundle format, String text) {
        logfile.getBundleStringLogOut(TAG + "===addTextOnlyLeft", format);
        logfile.printLog(TAG + "===addTextOnlyLeft, text:" + text);
        if (this.mPrinter == null) {
            logfile.printLog(TAG + "===mPrinter is NULL");
        } else if (!TextUtils.isEmpty(text)) {
            int fontStyle = 0;
            int nowrap = 1;
            int xPoint = 0;
            int fontsize = 1;
            int font = 1;
            int lineHeight = 5;
            int align = 0;
            boolean fontBold = false;
            String fontName = "";
            if (!this.initPage) {
                this.mPrinter.setupPage(384, -1);
                this.initPage = true;
            }

            if (this.currentYPoint == 0) {
                this.currentYPoint += 5;
            }

            if (format != null) {
                font = format.getInt("font", 1);
                fontName = format.getString("fontName", fontName_default);
                fontBold = format.getBoolean("fontBold", false);
                lineHeight = format.getInt("lineHeight", 5);
            }

            fontName = this.checkFontFileExist(fontName);

            if (font == 0) {
                fontsize = 16;
            } else if (font == 1) {
                fontsize = 24;
            } else if (font == 2) {
                fontsize = 32;
            } else {
                fontsize = 24;
            }

            if (format != null && format.containsKey("fontSize")) {
                int fontsize2 = format.getInt("fontSize", 0);
                if (fontsize2 > 0) {
                    fontsize = fontsize2;
                }
            }

            if (fontBold) {
                fontStyle = 1;
            }

            if (this.enablePrintY) {
                logfile.printLog(TAG + "===currentYPoint=" + this.currentYPoint);
            }

            this.currentYPoint += this.mPrinter.drawTextEx(text, 0, this.currentYPoint, 384, -1, fontName, fontsize, 0, fontStyle, nowrap) + lineHeight;
        }
    }

    public void addTextLeft_Right(String textLeft, String textRight, int font, boolean fontBold) {
        logfile.printLog(TAG + "===addTextLeft_Right, textLeft:" + textLeft + ", textRight:" + textRight);
        if (this.mPrinter != null) {
            if (!this.initPage) {
                this.mPrinter.setupPage(384, -1);
                this.initPage = true;
            }

            int leftHeight = this.currentYPoint;
            int rightHeight = this.currentYPoint;
            int fontsize = 1;
            int lineHeight = 5;
            if (font == 0) {
                fontsize = 16;
            } else if (font == 1) {
                fontsize = 24;
            } else if (font == 2) {
                fontsize = 32;
            } else {
                fontsize = 24;
            }

            int maxLine = 187;
            int fontStyle = 0;
            int xPoint = 0;
            int nowrap = 1;
            String text = "";
            String fontName = fontName_default;
            if (fontBold) {
                fontStyle = 1;
            }

            fontName = this.checkFontFileExist(fontName);
            StringBuffer stringBufferLeft = new StringBuffer();
            StringBuffer stringBufferRight = new StringBuffer();
            int maxLineLeft = 0;
            int maxLineRight = 0;
            float widthLeft = 0.0F;
            float widthRight = 0.0F;
            Paint mPaint = this.getPaintCache(fontsize, fontBold, false, fontName);

            int a;
            float nexWidth;
            String nextStr;
            for(a = 0; a < textLeft.length(); ++a) {
                stringBufferLeft.append(textLeft.charAt(a));
                widthLeft = mPaint.measureText(stringBufferLeft.toString());
                nexWidth = 0.0F;
                if (a < text.length() - 1) {
                    nextStr = String.valueOf(text.charAt(a + 1));
                    if (!TextUtils.isEmpty(nextStr)) {
                        nexWidth = mPaint.measureText(nextStr);
                    }
                }

                widthLeft += nexWidth;
            }

            for(a = 0; a < textRight.length(); ++a) {
                stringBufferRight.append(textRight.charAt(a));
                widthRight = mPaint.measureText(stringBufferRight.toString());
                nexWidth = 0.0F;
                if (a < text.length() - 1) {
                    nextStr = String.valueOf(text.charAt(a + 1));
                    if (!TextUtils.isEmpty(nextStr)) {
                        nexWidth = mPaint.measureText(nextStr);
                    }
                }

                widthRight += nexWidth;
            }

            if (widthLeft + widthRight < 374.0F) {
                maxLineLeft = (int)widthLeft;
                maxLineRight = 384 - maxLineLeft - 5;
                if (this.enablePrintY) {
                    logfile.printLog(TAG + "===leftHeight=" + leftHeight);
                }

                leftHeight += this.mPrinter.drawTextEx(stringBufferLeft.toString(), xPoint, leftHeight, maxLineLeft, -1, fontName, fontsize, 0, fontStyle, nowrap) + lineHeight;
                stringBufferLeft.setLength(0);
                xPoint = (int)(384.0F - widthRight) - 5;
                if (this.enablePrintY) {
                    logfile.printLog(TAG + "===rightHeight=" + rightHeight);
                }

                rightHeight += this.mPrinter.drawTextEx(stringBufferRight.toString(), xPoint, rightHeight, maxLineRight, -1, fontName, fontsize, 0, fontStyle, nowrap) + lineHeight;
                stringBufferRight.setLength(0);
            } else {
                StringBuffer stringBuffer = new StringBuffer();
                String lastStr;
                float width;
                if (!TextUtils.isEmpty(textLeft)) {
                    text = textLeft;

                    for(a = 0; a < text.length(); ++a) {
                        stringBuffer.append(text.charAt(a));
                        width = mPaint.measureText(stringBuffer.toString());
                        nexWidth = 0.0F;
                        if (a < text.length() - 1) {
                            lastStr = String.valueOf(text.charAt(a + 1));
                            if (!TextUtils.isEmpty(lastStr)) {
                                nexWidth = mPaint.measureText(lastStr);
                            }
                        }

                        if (width + nexWidth > (float)maxLine) {
                            if (this.enablePrintY) {
                                logfile.printLog(TAG + "===leftHeight=" + leftHeight);
                            }

                            leftHeight += this.mPrinter.drawTextEx(stringBuffer.toString(), xPoint, leftHeight, maxLine, -1, fontName, fontsize, 0, fontStyle, nowrap) + lineHeight;
                            stringBuffer.setLength(0);
                        } else if (a == text.length() - 1) {
                            lastStr = stringBuffer.toString();
                            if (TextUtils.isEmpty(lastStr)) {
                                return;
                            }

                            if (this.enablePrintY) {
                                logfile.printLog(TAG + "===leftHeight=" + leftHeight);
                            }

                            leftHeight += this.mPrinter.drawTextEx(stringBuffer.toString(), xPoint, leftHeight, maxLine, -1, fontName, fontsize, 0, fontStyle, nowrap) + lineHeight;
                        }
                    }
                }

                stringBuffer.setLength(0);
                xPoint = maxLine + 10;
                if (!TextUtils.isEmpty(textRight)) {
                    text = textRight;

                    for(a = 0; a < text.length(); ++a) {
                        stringBuffer.append(text.charAt(a));
                        width = mPaint.measureText(stringBuffer.toString());
                        nexWidth = 0.0F;
                        if (a < text.length() - 1) {
                            lastStr = String.valueOf(text.charAt(a + 1));
                            if (!TextUtils.isEmpty(lastStr)) {
                                nexWidth = mPaint.measureText(lastStr);
                            }
                        }

                        if (width + nexWidth > (float)maxLine) {
                            if (this.enablePrintY) {
                                logfile.printLog(TAG + "===rightHeight=" + rightHeight);
                            }

                            rightHeight += this.mPrinter.drawTextEx(stringBuffer.toString(), xPoint, rightHeight, maxLine, -1, fontName, fontsize, 0, fontStyle, nowrap) + lineHeight;
                            stringBuffer.setLength(0);
                        } else if (a == text.length() - 1) {
                            lastStr = stringBuffer.toString();
                            if (TextUtils.isEmpty(lastStr)) {
                                return;
                            }

                            xPoint = (int)(384.0F - width) - 5;
                            if (this.enablePrintY) {
                                logfile.printLog(TAG + "===rightHeight=" + rightHeight);
                            }

                            rightHeight += this.mPrinter.drawTextEx(stringBuffer.toString(), xPoint, rightHeight, maxLine, -1, fontName, fontsize, 0, fontStyle, nowrap) + lineHeight;
                        }
                    }
                }
            }

            if (leftHeight > rightHeight) {
                this.currentYPoint = leftHeight;
            } else {
                this.currentYPoint = rightHeight;
            }

        }
    }

    public void addTextLeft_Right(Bundle format, String textLeft, String textRight) {
        logfile.getBundleStringLogOut(TAG + "===addTextLeft_Right", format);
        logfile.printLog(TAG + "===addTextLeft_Right, textLeft:" + textLeft + ", textRight:" + textRight);
        if (this.mPrinter != null) {
            if (!this.initPage) {
                this.mPrinter.setupPage(384, -1);
                this.initPage = true;
            }

            int fontStyle = 0;
            int nowrap = 1;
            int xPoint = 0;
            int fontsize = 1;
            int font = 1;
            boolean fontBold = false;
            int lineHeight = 5;
            String fontName = "";
            if (format != null) {
                font = format.getInt("font", 1);
                fontName = format.getString("fontName", fontName_default);
                fontBold = format.getBoolean("fontBold", false);
                lineHeight = format.getInt("lineHeight", 5);
            }

            fontName = this.checkFontFileExist(fontName);
            if (font == 0) {
                fontsize = 16;
            } else if (font == 1) {
                fontsize = 24;
            } else if (font == 2) {
                fontsize = 32;
            } else {
                fontsize = 24;
            }

            if (format != null && format.containsKey("fontSize")) {
                int fontsize2 = format.getInt("fontSize", 0);
                if (fontsize2 > 0) {
                    fontsize = fontsize2;
                }
            }

            int maxLine = 187;
            String text = "";
            if (fontBold) {
                fontStyle = 1;
            }

            int leftHeight = this.currentYPoint;
            int rightHeight = this.currentYPoint;
            StringBuffer stringBufferLeft = new StringBuffer();
            StringBuffer stringBufferRight = new StringBuffer();
            int maxLineLeft = 0;
            int maxLineRight = 0;
            float widthLeft = 0.0F;
            float widthRight = 0.0F;
            Paint mPaint = this.getPaintCache(fontsize, fontBold, false, fontName);

            int a;
            float nexWidth;
            String nextStr;
            for(a = 0; a < textLeft.length(); ++a) {
                stringBufferLeft.append(textLeft.charAt(a));
                widthLeft = mPaint.measureText(stringBufferLeft.toString());
                nexWidth = 0.0F;
                if (a < text.length() - 1) {
                    nextStr = String.valueOf(text.charAt(a + 1));
                    if (!TextUtils.isEmpty(nextStr)) {
                        nexWidth = mPaint.measureText(nextStr);
                    }
                }

                widthLeft += nexWidth;
            }

            logfile.printLog("widthLeft:" + widthLeft);

            for(a = 0; a < textRight.length(); ++a) {
                stringBufferRight.append(textRight.charAt(a));
                widthRight = mPaint.measureText(stringBufferRight.toString());
                nexWidth = 0.0F;
                if (a < text.length() - 1) {
                    nextStr = String.valueOf(text.charAt(a + 1));
                    if (!TextUtils.isEmpty(nextStr)) {
                        nexWidth = mPaint.measureText(nextStr);
                    }
                }

                widthRight += nexWidth;
            }

            logfile.printLog("widthRight:" + widthRight);
            if (widthLeft + widthRight < 374.0F) {
                maxLineLeft = (int)widthLeft;
                maxLineRight = 384 - maxLineLeft - 5;
                if (this.enablePrintY) {
                    logfile.printLog(TAG + "===leftHeight=" + leftHeight);
                }

                leftHeight += this.mPrinter.drawTextEx(stringBufferLeft.toString(), xPoint, leftHeight, maxLineLeft, -1, fontName, fontsize, 0, fontStyle, nowrap) + lineHeight;
                stringBufferLeft.setLength(0);
                xPoint = (int)(384.0F - widthRight) - 5;
                maxLineRight = 384 - maxLineLeft - 5;
                if (this.enablePrintY) {
                    logfile.printLog(TAG + "===rightHeight=" + rightHeight);
                }

                rightHeight += this.mPrinter.drawTextEx(stringBufferRight.toString(), xPoint, rightHeight, maxLineRight, -1, fontName, fontsize, 0, fontStyle, nowrap) + lineHeight;
                stringBufferRight.setLength(0);
            } else {
                StringBuffer stringBuffer = new StringBuffer();
                String lastStr;
                float width;
                if (!TextUtils.isEmpty(textLeft)) {
                    text = textLeft;

                    for(a = 0; a < text.length(); ++a) {
                        stringBuffer.append(text.charAt(a));
                        width = mPaint.measureText(stringBuffer.toString());
                        nexWidth = 0.0F;
                        if (a < text.length() - 1) {
                            lastStr = String.valueOf(text.charAt(a + 1));
                            if (!TextUtils.isEmpty(lastStr)) {
                                nexWidth = mPaint.measureText(lastStr);
                            }
                        }

                        if (width + nexWidth > (float)maxLine) {
                            if (this.enablePrintY) {
                                logfile.printLog(TAG + "===leftHeight=" + leftHeight);
                            }

                            leftHeight += this.mPrinter.drawTextEx(stringBuffer.toString(), xPoint, leftHeight, maxLine, -1, fontName, fontsize, 0, fontStyle, nowrap) + lineHeight;
                            stringBuffer.setLength(0);
                        } else if (a == text.length() - 1) {
                            lastStr = stringBuffer.toString();
                            if (TextUtils.isEmpty(lastStr)) {
                                return;
                            }

                            if (this.enablePrintY) {
                                logfile.printLog(TAG + "===leftHeight=" + leftHeight);
                            }

                            leftHeight += this.mPrinter.drawTextEx(stringBuffer.toString(), xPoint, leftHeight, maxLine, -1, fontName, fontsize, 0, fontStyle, nowrap) + lineHeight;
                        }
                    }
                }

                stringBuffer.setLength(0);
                xPoint = maxLine + 10;
                if (!TextUtils.isEmpty(textRight)) {
                    text = textRight;

                    for(a = 0; a < text.length(); ++a) {
                        stringBuffer.append(text.charAt(a));
                        width = mPaint.measureText(stringBuffer.toString());
                        nexWidth = 0.0F;
                        if (a < text.length() - 1) {
                            lastStr = String.valueOf(text.charAt(a + 1));
                            if (!TextUtils.isEmpty(lastStr)) {
                                nexWidth = mPaint.measureText(lastStr);
                            }
                        }

                        if (width + nexWidth > (float)maxLine) {
                            if (this.enablePrintY) {
                                logfile.printLog(TAG + "===rightHeight=" + rightHeight);
                            }

                            rightHeight += this.mPrinter.drawTextEx(stringBuffer.toString(), xPoint, rightHeight, maxLine, -1, fontName, fontsize, 0, fontStyle, nowrap) + lineHeight;
                            stringBuffer.setLength(0);
                        } else if (a == text.length() - 1) {
                            lastStr = stringBuffer.toString();
                            if (TextUtils.isEmpty(lastStr)) {
                                return;
                            }

                            xPoint = (int)(384.0F - width) - 5;
                            if (this.enablePrintY) {
                                logfile.printLog(TAG + "===rightHeight=" + rightHeight);
                            }

                            rightHeight += this.mPrinter.drawTextEx(stringBuffer.toString(), xPoint, rightHeight, maxLine, -1, fontName, fontsize, 0, fontStyle, nowrap) + lineHeight;
                        }
                    }
                }
            }

            if (leftHeight > rightHeight) {
                this.currentYPoint = leftHeight;
            } else {
                this.currentYPoint = rightHeight;
            }

        }
    }

    public void addTextLeft_Center_Right(String textLeft, String textCenter, String textRight, int font, boolean fontBold) {
        logfile.printLog(TAG + "===addTextLeft_Center_Right, textLeft:" + textLeft + ", textCenter:" + textCenter + ", textRight:" + textRight + ", font:" + font + ", fontBold:" + fontBold);
        if (this.mPrinter != null) {
            if (!this.initPage) {
                this.mPrinter.setupPage(384, -1);
                this.initPage = true;
            }

            int leftHeight = this.currentYPoint;
            int centerHeight = this.currentYPoint;
            int rightHeight = this.currentYPoint;
            int fontsize = 1;
            int lineHeight = 5;
            if (font == 0) {
                fontsize = 16;
            } else if (font == 1) {
                fontsize = 24;
            } else if (font == 2) {
                fontsize = 32;
            } else {
                fontsize = 24;
            }

            int maxLine = 128;
            int fontStyle = 0;
            int xPoint = 0;
            int nowrap = 1;
            String fontName = fontName_default;
            String text = "";
            if (fontBold) {
                fontStyle = 1;
            }

            fontName = this.checkFontFileExist(fontName);
            StringBuffer stringBuffer = new StringBuffer();
            Paint mPaint;
            int a;
            float width;
            float nexWidth;
            String lastStr;
            if (!TextUtils.isEmpty(textLeft)) {
                text = textLeft;
                mPaint = this.getPaintCache(fontsize, fontBold, false, fontName);

                for(a = 0; a < text.length(); ++a) {
                    stringBuffer.append(text.charAt(a));
                    width = mPaint.measureText(stringBuffer.toString());
                    nexWidth = 0.0F;
                    if (a < text.length() - 1) {
                        lastStr = String.valueOf(text.charAt(a + 1));
                        if (!TextUtils.isEmpty(lastStr)) {
                            nexWidth = mPaint.measureText(lastStr);
                        }
                    }

                    if (width + nexWidth > (float)maxLine) {
                        if (this.enablePrintY) {
                            logfile.printLog(TAG + "===leftHeight=" + leftHeight);
                        }

                        leftHeight += this.mPrinter.drawTextEx(stringBuffer.toString(), 0, leftHeight, maxLine, -1, fontName, fontsize, 0, fontStyle, nowrap) + lineHeight;
                        stringBuffer.setLength(0);
                    } else if (a == text.length() - 1) {
                        lastStr = stringBuffer.toString();
                        if (TextUtils.isEmpty(lastStr)) {
                            return;
                        }

                        if (this.enablePrintY) {
                            logfile.printLog(TAG + "===leftHeight=" + leftHeight);
                        }

                        leftHeight += this.mPrinter.drawTextEx(stringBuffer.toString(), xPoint, leftHeight, maxLine, -1, fontName, fontsize, 0, fontStyle, nowrap) + lineHeight;
                    }
                }
            }

            stringBuffer.setLength(0);
            if (!TextUtils.isEmpty(textCenter)) {
                text = textCenter;
                mPaint = this.getPaintCache(fontsize, fontBold, false, fontName);

                for(a = 0; a < text.length(); ++a) {
                    stringBuffer.append(text.charAt(a));
                    width = mPaint.measureText(stringBuffer.toString());
                    nexWidth = 0.0F;
                    if (a < text.length() - 1) {
                        lastStr = String.valueOf(text.charAt(a + 1));
                        if (!TextUtils.isEmpty(lastStr)) {
                            nexWidth = mPaint.measureText(lastStr);
                        }
                    }

                    if (width + nexWidth > (float)maxLine) {
                        if (this.enablePrintY) {
                            logfile.printLog(TAG + "===centerHeight=" + centerHeight);
                        }

                        centerHeight += this.mPrinter.drawTextEx(stringBuffer.toString(), 130, centerHeight, maxLine, -1, fontName, fontsize, 0, fontStyle, nowrap) + lineHeight;
                        stringBuffer.setLength(0);
                    } else if (a == text.length() - 1) {
                        lastStr = stringBuffer.toString();
                        if (TextUtils.isEmpty(lastStr)) {
                            return;
                        }

                        xPoint = (int)(384.0F - width) / 2;
                        if (this.enablePrintY) {
                            logfile.printLog(TAG + "===centerHeight=" + centerHeight);
                        }

                        centerHeight += this.mPrinter.drawTextEx(stringBuffer.toString(), xPoint, centerHeight, maxLine, -1, fontName, fontsize, 0, fontStyle, nowrap) + lineHeight;
                    }
                }
            }

            stringBuffer.setLength(0);
            if (!TextUtils.isEmpty(textRight)) {
                text = textRight;
                mPaint = this.getPaintCache(fontsize, fontBold, false, fontName_default);

                for(a = 0; a < text.length(); ++a) {
                    stringBuffer.append(text.charAt(a));
                    width = mPaint.measureText(stringBuffer.toString());
                    nexWidth = 0.0F;
                    if (a < text.length() - 1) {
                        lastStr = String.valueOf(text.charAt(a + 1));
                        if (!TextUtils.isEmpty(lastStr)) {
                            nexWidth = mPaint.measureText(lastStr);
                        }
                    }

                    if (width + nexWidth > (float)maxLine) {
                        if (this.enablePrintY) {
                            logfile.printLog(TAG + "===rightHeight=" + rightHeight);
                        }

                        rightHeight += this.mPrinter.drawTextEx(stringBuffer.toString(), 260, rightHeight, maxLine, -1, fontName, fontsize, 0, fontStyle, nowrap) + 5;
                        stringBuffer.setLength(0);
                    } else if (a == text.length() - 1) {
                        lastStr = stringBuffer.toString();
                        if (TextUtils.isEmpty(lastStr)) {
                            return;
                        }

                        xPoint = (int)(384.0F - width) - 5;
                        if (this.enablePrintY) {
                            logfile.printLog(TAG + "===rightHeight=" + rightHeight);
                        }

                        rightHeight += this.mPrinter.drawTextEx(stringBuffer.toString(), xPoint, rightHeight, maxLine, -1, fontName, fontsize, 0, fontStyle, nowrap) + 5;
                    }
                }
            }

            this.currentYPoint = leftHeight;
            if (leftHeight < centerHeight) {
                this.currentYPoint = centerHeight;
            }

            if (this.currentYPoint < rightHeight) {
                this.currentYPoint = rightHeight;
            }

        }
    }

    public void addTextLeft_Center_Right(Bundle format, String textLeft, String textCenter, String textRight) {
        logfile.getBundleStringLogOut(TAG + "===addTextLeft_Center_Right", format);
        logfile.printLog(TAG + "===addTextLeft_Center_Right, textLeft:" + textLeft + ", textCenter:" + textCenter + ", textRight:" + textRight);
        if (this.mPrinter == null) {
            logfile.printLog(TAG + "===mPrinter is NULL");
        } else {
            if (!this.initPage) {
                this.mPrinter.setupPage(384, -1);
                this.initPage = true;
            }

            int fontStyle = 0;
            int nowrap = 1;
            int xPoint = 0;
            int fontsize = 1;
            int font = 1;
            boolean fontBold = false;
            String fontName = "";
            int leftHeight = this.currentYPoint;
            int centerHeight = this.currentYPoint;
            int rightHeight = this.currentYPoint;
            int lineHeight = 5;
            if (format != null) {
                font = format.getInt("font", 1);
                fontName = format.getString("fontName", fontName_default);
                fontBold = format.getBoolean("fontBold", false);
                lineHeight = format.getInt("lineHeight", 5);
            }

            fontName = this.checkFontFileExist(fontName);
            if (font == 0) {
                fontsize = 16;
            } else if (font == 1) {
                fontsize = 24;
            } else if (font == 2) {
                fontsize = 32;
            } else {
                fontsize = 24;
            }

            if (format != null && format.containsKey("fontSize")) {
                int fontsize2 = format.getInt("fontSize", 0);
                if (fontsize2 > 0) {
                    fontsize = fontsize2;
                }
            }

            int maxLine = 128;
            String text = "";
            if (fontBold) {
                fontStyle = 1;
            }

            StringBuffer stringBuffer = new StringBuffer();
            Paint mPaint;
            int a;
            float width;
            float nexWidth;
            String lastStr;
            if (!TextUtils.isEmpty(textLeft) && !TextUtils.isEmpty(textLeft)) {
                text = textLeft;
                mPaint = this.getPaintCache(fontsize, fontBold, false, fontName);

                for(a = 0; a < text.length(); ++a) {
                    stringBuffer.append(text.charAt(a));
                    width = mPaint.measureText(stringBuffer.toString());
                    nexWidth = 0.0F;
                    if (a < text.length() - 1) {
                        lastStr = String.valueOf(text.charAt(a + 1));
                        if (!TextUtils.isEmpty(lastStr)) {
                            nexWidth = mPaint.measureText(lastStr);
                        }
                    }

                    if (width + nexWidth > (float)maxLine) {
                        if (this.enablePrintY) {
                            logfile.printLog(TAG + "===leftHeight=" + leftHeight);
                        }

                        leftHeight += this.mPrinter.drawTextEx(stringBuffer.toString(), 0, leftHeight, maxLine, -1, fontName, fontsize, 0, fontStyle, nowrap) + lineHeight;
                        stringBuffer.setLength(0);
                    } else if (a == text.length() - 1) {
                        lastStr = stringBuffer.toString();
                        if (TextUtils.isEmpty(lastStr)) {
                            return;
                        }

                        if (this.enablePrintY) {
                            logfile.printLog(TAG + "===leftHeight=" + leftHeight);
                        }

                        leftHeight += this.mPrinter.drawTextEx(stringBuffer.toString(), xPoint, leftHeight, maxLine, -1, fontName, fontsize, 0, fontStyle, nowrap) + lineHeight;
                    }
                }
            }

            stringBuffer.setLength(0);
            if (!TextUtils.isEmpty(textCenter)) {
                text = textCenter;
                mPaint = this.getPaintCache(fontsize, fontBold, false, fontName);

                for(a = 0; a < text.length(); ++a) {
                    stringBuffer.append(text.charAt(a));
                    width = mPaint.measureText(stringBuffer.toString());
                    nexWidth = 0.0F;
                    if (a < text.length() - 1) {
                        lastStr = String.valueOf(text.charAt(a + 1));
                        if (!TextUtils.isEmpty(lastStr)) {
                            nexWidth = mPaint.measureText(lastStr);
                        }
                    }

                    if (width + nexWidth > (float)maxLine) {
                        if (this.enablePrintY) {
                            logfile.printLog(TAG + "===centerHeight=" + centerHeight);
                        }

                        centerHeight += this.mPrinter.drawTextEx(stringBuffer.toString(), 130, centerHeight, maxLine, -1, fontName, fontsize, 0, fontStyle, nowrap) + lineHeight;
                        stringBuffer.setLength(0);
                    } else if (a == text.length() - 1) {
                        lastStr = stringBuffer.toString();
                        if (TextUtils.isEmpty(lastStr)) {
                            return;
                        }

                        xPoint = (int)(384.0F - width) / 2;
                        if (this.enablePrintY) {
                            logfile.printLog(TAG + "===centerHeight=" + centerHeight);
                        }

                        centerHeight += this.mPrinter.drawTextEx(stringBuffer.toString(), xPoint, centerHeight, maxLine, -1, fontName, fontsize, 0, fontStyle, nowrap) + lineHeight;
                    }
                }
            }

            stringBuffer.setLength(0);
            if (!TextUtils.isEmpty(textRight)) {
                text = textRight;
                mPaint = this.getPaintCache(fontsize, fontBold, false, fontName_default);

                for(a = 0; a < text.length(); ++a) {
                    stringBuffer.append(text.charAt(a));
                    width = mPaint.measureText(stringBuffer.toString());
                    nexWidth = 0.0F;
                    if (a < text.length() - 1) {
                        lastStr = String.valueOf(text.charAt(a + 1));
                        if (!TextUtils.isEmpty(lastStr)) {
                            nexWidth = mPaint.measureText(lastStr);
                        }
                    }

                    if (width + nexWidth > (float)maxLine) {
                        if (this.enablePrintY) {
                            logfile.printLog(TAG + "===rightHeight=" + rightHeight);
                        }

                        rightHeight += this.mPrinter.drawTextEx(stringBuffer.toString(), 260, rightHeight, maxLine, -1, fontName, fontsize, 0, fontStyle, nowrap) + lineHeight;
                        stringBuffer.setLength(0);
                    } else if (a == text.length() - 1) {
                        lastStr = stringBuffer.toString();
                        if (TextUtils.isEmpty(lastStr)) {
                            return;
                        }

                        xPoint = (int)(384.0F - width) - 5;
                        if (this.enablePrintY) {
                            logfile.printLog(TAG + "===rightHeight=" + rightHeight);
                        }

                        rightHeight += this.mPrinter.drawTextEx(stringBuffer.toString(), xPoint, rightHeight, maxLine, -1, fontName, fontsize, 0, fontStyle, nowrap) + lineHeight;
                    }
                }
            }

            this.currentYPoint = leftHeight;
            if (leftHeight < centerHeight) {
                this.currentYPoint = centerHeight;
            }

            if (this.currentYPoint < rightHeight) {
                this.currentYPoint = rightHeight;
            }

            logfile.printLog(TAG + "===currentYPoint:" + this.currentYPoint);
        }
    }

    public void feedLine(int lines) {
        logfile.printLog(TAG + "===feedLine lines:" + lines);

        for(int i = 0; i < lines; ++i) {
            this.addText((Bundle)null, " ");
        }

        if (lines == -1 || lines == 0) {
            this.feedLine = lines;
        }

    }

    public void addBlackLine() {
        this.currentYPoint += 5;
        if (this.enablePrintY) {
            logfile.printLog(TAG + "===currentYPoint=" + this.currentYPoint);
        }

        this.currentYPoint += this.mPrinter.drawLine(0, this.currentYPoint, 384, this.currentYPoint, 2) + 5;
    }

    public void paperFeed(int len) {
        logfile.printLog(TAG + "===paperFeed, len:" + len);
        if (this.mPrinter == null) {
            logfile.printLog(TAG + "===mPrinter is NULL");
        } else {
            this.mPrinter.paperFeed(len);
        }
    }

    public void addBitmap(Bitmap bitmap, int offset) {
        this.appendImage((Bundle)null, offset, bitmap);
    }

    public void addBarCode(Bundle format, String barcode) {
        logfile.printLog(TAG + "===addBarCode, barcode:" + barcode);
        int result = 1;
        if (this.mPrinter == null) {
            logfile.printLog(TAG + "===mPrinter is NULL");
        } else if (TextUtils.isEmpty(barcode)) {
            logfile.printLog(TAG + "===barcode is NULL");
        } else {
            if (!this.initPage) {
                this.mPrinter.setupPage(384, -1);
                this.initPage = true;
            }

            int width = format.getInt("width", 300);
            int height = format.getInt("height", 100);
            BarcodeFormat barcodeFormat = (BarcodeFormat)format.getSerializable("barcode_type");
            if (barcodeFormat == null) {
                barcodeFormat = BarcodeFormat.CODE_128;
            }

            Bitmap bitmap = EncodingHandler.creatBarcode(barcode, 1, height, false, 1, barcodeFormat);
            int sourceWidth = bitmap.getWidth();
            int sourceHeight = bitmap.getHeight();
            if (height > 0 && width > 0) {
                float scaleWidth = (float)width / (float)sourceWidth;
                float scaleHeight = (float)height / (float)sourceHeight;
                Matrix matrix = new Matrix();
                matrix.postScale(scaleWidth, scaleHeight);
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            }

            this.appendBitmap(bitmap, format.getInt("align"));
        }
    }

    public int appendBitmap(Bitmap bitmap, int alignment) {
        logfile.printLog(TAG + "===appendBitmap, alignment:" + alignment);
        int result = -1;
        if (this.mPrinter == null) {
            logfile.printLog(TAG + "===mPrinter is NULL");
            return result;
        } else if (bitmap == null) {
            logfile.printLog(TAG + "===bitmap is NULL");
            return result;
        } else {
            this.currentYPoint += 10;
            int xPoint = 0;
            int width = bitmap.getWidth();
            int heigth = bitmap.getHeight();
            if (alignment == 0) {
                if (this.enablePrintY) {
                    logfile.printLog(TAG + "===currentYPoint=" + this.currentYPoint);
                }

                result = this.mPrinter.drawBitmap(bitmap, xPoint, this.currentYPoint);
            } else if (alignment == 2) {
                if (width >= 384) {
                    xPoint = 0;
                } else {
                    xPoint = 384 - width;
                }

                if (this.enablePrintY) {
                    logfile.printLog(TAG + "===currentYPoint=" + this.currentYPoint);
                }

                result = this.mPrinter.drawBitmap(bitmap, xPoint, this.currentYPoint);
            } else if (alignment == 1) {
                if (width >= 384) {
                    xPoint = 0;
                } else {
                    xPoint = (384 - width) / 2;
                }

                if (this.enablePrintY) {
                    logfile.printLog(TAG + "===currentYPoint=" + this.currentYPoint);
                }

                result = this.mPrinter.drawBitmap(bitmap, xPoint, this.currentYPoint);
            }

            logfile.printLog(TAG + "===drawBitmap:" + result);
            if (result == 0) {
                this.currentYPoint += heigth + 10;
            }

            return result;
        }
    }

    public void addImage(Bundle format, byte[] imageData) {
        logfile.printLog(TAG + "===addImage");
        if (this.mPrinter == null) {
            logfile.printLog(TAG + "===mPrinter is NULL");
        } else if (imageData == null) {
            logfile.printLog(TAG + "===imageData is NULL");
        } else {
            logfile.getBundleStringLogOut(TAG + "===addImage", format);
            int align = 0;
            int width = 0;
            int height = 0;
            int offset = 0;
            if (format != null) {
                align = format.getInt("align", 0);
                width = format.getInt("width", 0);
                height = format.getInt("height", 0);
                offset = format.getInt("offset");
            }

            if (imageData != null && imageData.length > 0) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
                int sourceWidth = bitmap.getWidth();
                int sourceHeight = bitmap.getHeight();
                logfile.printLog(TAG + "===sourceWidth:" + sourceWidth);
                logfile.printLog(TAG + "===sourceHeight:" + sourceHeight);
                if (height > 0 && width > 0) {
                    float scaleWidth = (float)width / (float)sourceWidth;
                    float scaleHeight = (float)height / (float)sourceHeight;
                    Matrix matrix = new Matrix();
                    matrix.postScale(scaleWidth, scaleHeight);
                    bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                }

                if (bitmap != null) {
                    if (offset <= 0) {
                        offset = 0;
                        if (align == 0) {
                            offset = 0;
                        } else if (align == 1) {
                            offset = (384 - bitmap.getWidth()) / 2 - 8;
                        } else if (align == 2) {
                            offset = 384 - bitmap.getWidth();
                        }
                    }

                    this.appendImage(format, offset, bitmap);
                }

            }
        }
    }

    private int appendImage(Bundle format, int xPoint, Bitmap bitmap) {
        int result = -1;
        if (this.mPrinter == null) {
            logfile.printLog(TAG + "===mPrinter is NULL");
            return result;
        } else if (bitmap == null) {
            logfile.printLog(TAG + "===bitmap is NULL");
            return result;
        } else {
            if (!this.initPage) {
                this.mPrinter.setupPage(384, -1);
                this.initPage = true;
            }

            if (format != null) {
                String saveImagePath = format.getString("save_image_path", "");
                String saveImageName = format.getString("save_image_name", "");
                if (!TextUtils.isEmpty(saveImagePath)) {
                    FilesUtil.saveBitmap(bitmap, saveImagePath, saveImageName);
                }
            }

            int height = bitmap.getHeight();
            logfile.printLog(TAG + "===bitmap height:" + height);
            logfile.printLog(TAG + "===bitmap Width:" + bitmap.getWidth());
            if (this.enablePrintY) {
                logfile.printLog(TAG + "===currentYPoint=" + this.currentYPoint);
            }

            result = this.mPrinter.drawBitmap(bitmap, xPoint, this.currentYPoint);
            logfile.printLog(TAG + "===drawBitmap:" + result);

            try {
                bitmap.recycle();
                bitmap = null;
            } catch (Exception var7) {
                Exception e = var7;
                e.printStackTrace();
            }

            if (result == 0) {
                this.currentYPoint += height;
            }

            return result;
        }
    }

    public void addImageWithText(Bundle format, byte[] imageData) {
        logfile.printLog(TAG + "===addImage");
        if (this.mPrinter == null) {
            logfile.printLog(TAG + "===mPrinter is NULL");
        } else if (imageData == null) {
            logfile.printLog(TAG + "===imageData is NULL");
        } else {
            logfile.getBundleStringLogOut(TAG + "===addImage", format);
            int align = 0;
            int width = 0;
            int height = 0;
            int offset = 0;
            if (format != null) {
                align = format.getInt("align", 0);
                width = format.getInt("width", 0);
                height = format.getInt("height", 0);
                offset = format.getInt("offset");
            }

            if (imageData != null && imageData.length > 0) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
                int sourceWidth = bitmap.getWidth();
                int sourceHeight = bitmap.getHeight();
                if (height > 0 && width > 0) {
                    float scaleWidth = (float)width / (float)sourceWidth;
                    float scaleHeight = (float)height / (float)sourceHeight;
                    Matrix matrix = new Matrix();
                    matrix.postScale(scaleWidth, scaleHeight);
                    bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                }

                if (bitmap != null) {
                    if (offset <= 0) {
                        offset = 0;
                        if (align == 0) {
                            offset = 0;
                        } else if (align == 1) {
                            offset = (384 - bitmap.getWidth()) / 2 - 8;
                        } else if (align == 2) {
                            offset = 384 - bitmap.getWidth();
                        }
                    }

                    this.appendImageWithText(format, offset, bitmap);
                }

            }
        }
    }

    private int appendImageWithText(Bundle format, int xPoint, Bitmap bitmap) {
        int result = -1;
        if (this.mPrinter == null) {
            logfile.printLog(TAG + "===mPrinter is NULL");
            return result;
        } else if (bitmap == null) {
            logfile.printLog(TAG + "===bitmap is NULL");
            return result;
        } else {
            if (!this.initPage) {
                this.mPrinter.setupPage(384, -1);
                this.initPage = true;
            }

            int heigth = bitmap.getHeight();
            logfile.printLog(TAG + "===bitmap heigth:" + heigth);
            if (this.enablePrintY) {
                logfile.printLog(TAG + "===currentYPoint=" + this.currentYPoint);
            }

            result = this.mPrinter.drawBitmap(bitmap, xPoint, this.currentYPoint);
            logfile.printLog(TAG + "===drawBitmap:" + result);

            try {
                bitmap.recycle();
                bitmap = null;
            } catch (Exception var9) {
                Exception e = var9;
                e.printStackTrace();
            }

            if (format != null && format.containsKey("text")) {
                String text = format.getString("text");
                int YAlign = format.getInt("YAlign", 1);
                Bundle textFormat = new Bundle();
                textFormat.putInt("font", format.getInt("font", 1));
                textFormat.putString("fontName", format.getString("fontName", fontName_default));
                textFormat.putBoolean("fontBold", format.getBoolean("fontBold", false));
                textFormat.putInt("lineHeight", format.getInt("lineHeight", 0));
                textFormat.putInt("align", 2);
                if (YAlign == 0) {
                    this.addTextInBitmap(textFormat, text, xPoint);
                } else if (YAlign == 1) {
                    this.currentYPoint += heigth / 2 - 15;
                    this.addTextInBitmap(textFormat, text, xPoint);
                } else if (YAlign == 2) {
                    this.currentYPoint += heigth - 25;
                    this.addTextInBitmap(textFormat, text, xPoint);
                }
            }

            if (result == 0) {
                this.currentYPoint += heigth;
            }

            return result;
        }
    }

    public void addTextInBitmap(Bundle format, String text, int bitmapXPoint) {
        logfile.getBundleStringLogOut(TAG + "===addText", format);
        logfile.printLog(TAG + "===addText, text:" + text);
        if (this.mPrinter == null) {
            logfile.printLog(TAG + "===mPrinter is NULL");
        } else if (!TextUtils.isEmpty(text)) {
            int fontStyle = 0;
            int nowrap = 1;
            int xPoint = 0;
            int fontsize = 1;
            int font = 1;
            int lineHeight = 5;
            int align = 0;
            boolean fontBold = false;
            String fontName = "";
            if (!this.initPage) {
                this.mPrinter.setupPage(384, -1);
                this.initPage = true;
            }

            if (this.currentYPoint == 0) {
                this.currentYPoint += 5;
            }

            if (format != null) {
                font = format.getInt("font", 1);
                align = format.getInt("align", 0);
                fontName = format.getString("fontName", fontName_default);
                fontBold = format.getBoolean("fontBold", false);
            }

            fontName = this.checkFontFileExist(fontName);
            if (font == 0) {
                fontsize = 16;
            } else if (font == 1) {
                fontsize = 24;
            } else if (font == 2) {
                fontsize = 32;
            } else {
                fontsize = 24;
            }

            int maxLine;
            if (format != null && format.containsKey("fontSize")) {
                maxLine = format.getInt("fontSize", 0);
                if (maxLine > 0) {
                    fontsize = maxLine;
                }
            }

            if (fontBold) {
                fontStyle = 1;
            }

            maxLine = bitmapXPoint - 20;
            int YPoint = this.currentYPoint;
            StringBuffer stringBuffer = new StringBuffer();
            Paint mPaint = this.getPaintCache(fontsize, fontBold, false, fontName);

            for(int a = 0; a < text.length(); ++a) {
                stringBuffer.append(text.charAt(a));
                float width = mPaint.measureText(stringBuffer.toString());
                float nexWidth = 0.0F;
                String lastStr;
                if (a < text.length() - 1) {
                    lastStr = String.valueOf(text.charAt(a + 1));
                    if (!TextUtils.isEmpty(lastStr)) {
                        nexWidth = mPaint.measureText(lastStr);
                    }
                }

                if (width + nexWidth > (float)maxLine) {
                    if (this.enablePrintY) {
                        logfile.printLog(TAG + "===YPoint=" + YPoint);
                    }

                    YPoint += this.mPrinter.drawTextEx(stringBuffer.toString(), 0, YPoint, 384, -1, fontName, fontsize, 0, fontStyle, nowrap) + lineHeight;
                    stringBuffer.setLength(0);
                } else if (a == text.length() - 1) {
                    lastStr = stringBuffer.toString();
                    if (TextUtils.isEmpty(lastStr)) {
                        return;
                    }

                    if (1 == align) {
                        xPoint = (int)((float)maxLine - width) / 2;
                    } else if (2 == align) {
                        xPoint = (int)((float)maxLine - width);
                    }

                    if (this.enablePrintY) {
                        logfile.printLog(TAG + "===YPoint=" + YPoint);
                    }

                    YPoint += this.mPrinter.drawTextEx(stringBuffer.toString(), xPoint, YPoint, 384, -1, fontName, fontsize, 0, fontStyle, nowrap) + lineHeight;
                }
            }

        }
    }

    public void addQrCode(Bundle format, String qrCode) {
        logfile.printLog(TAG + "===addQrCode, qrCode:" + qrCode);
        if (this.mPrinter == null) {
            logfile.printLog(TAG + "===mPrinter is NULL");
        } else if (TextUtils.isEmpty(qrCode)) {
            logfile.printLog(TAG + qrCode);
        } else {
            int offset = 0;
            int height = 100;
            int align = 0;
            if (format != null) {
                offset = format.getInt("offset", 0);
                if (format.containsKey("expectedHeight")) {
                    height = format.getInt("expectedHeight", 100);
                } else {
                    height = format.getInt("height", 100);
                }

                align = format.getInt("align", 0);
            }

            if (height >= 384) {
                height = 384;
                offset = 0;
            }

            try {
                if (qrCode != null && qrCode.length() != 0) {
                    Bitmap qrCodeBmp = QRCodeUtil.createQRCodeBitmap(qrCode, height, height, "UTF-8", "L", "0", -16777216, -1);
                    if (offset <= 0) {
                        offset = 0;
                        if (align == 0) {
                            offset = 0;
                        } else if (align == 1) {
                            offset = (384 - qrCodeBmp.getWidth()) / 2 - 8;
                        } else if (align == 2) {
                            offset = 384 - qrCodeBmp.getWidth();
                        }
                    }

                    if (qrCodeBmp != null) {
                        this.appendImage(format, offset, qrCodeBmp);
                    }
                }
            } catch (Exception var8) {
                Exception e = var8;
                e.printStackTrace();
            }

        }
    }

    public void setGray(int grayLevel) {
        logfile.printLog(TAG + "===setGray, grayLevel:" + grayLevel);
        if (this.mPrinter == null) {
            logfile.printLog(TAG + "===mPrinter is NULL");
        } else {
            if (grayLevel < -6) {
                grayLevel = -6;
            }

            if (grayLevel > 6) {
                grayLevel = 6;
            }

            this.mPrinter.setGrayLevel(grayLevel);
        }
    }

    public int startPrint() {
        logfile.printLog(TAG + "===startPrint");
        if (this.mPrinter == null) {
            logfile.printLog(TAG + "===mPrinter is NULL");
            return -1;
        } else {
            this.mStatus = 247;
            if (this.currentYPoint < 0) {
                return this.mStatus == 247 ? this.mStatus : -1;
            } else {
                if (this.feedLine == 0) {
                    this.feedLine(3);
                }

                logfile.printLog(TAG + "===currentYPoint=" + this.currentYPoint);
                int status = this.mPrinter.getStatus();
                logfile.printLog(TAG + "===doInPrint.getStatus() status1:" + status);
                int result = this.mPrinter.printPage(0);
                logfile.printLog(TAG + "===printPage:" + result);
                this.feedLine = 0;
                this.initPage = true;
                this.mPrinter.setupPage(-1, -1);
                this.mPrinter.clearPage();
                status = this.mPrinter.getStatus();
                logfile.printLog(TAG + "===doInPrint.getStatus() status2 = " + status);

                while(status == -4 || status == -6) {
                    status = this.mPrinter.getStatus();
                    if (status != -4 && status != -6) {
                        break;
                    }
                }

                this.currentYPoint = 0;
                logfile.printLog(TAG + "===doInPrint.getStatus() status4 = " + status);
                if (status != 0) {
                    if (status == -1) {
                        this.mStatus = 240;
                    } else if (status == -2) {
                        this.mStatus = 243;
                    } else if (status == -3) {
                        this.mStatus = 225;
                    } else if (status == -4) {
                        this.mStatus = 247;
                    } else if (status == -256) {
                        this.mStatus = 251;
                    } else if (status == -257) {
                        this.mStatus = 242;
                    } else {
                        this.mStatus = status;
                    }
                } else {
                    this.mStatus = 0;
                    if (result == -1) {
                        this.mStatus = 240;
                    } else if (result == -2) {
                        this.mStatus = 243;
                    } else if (result == -3) {
                        this.mStatus = 225;
                    } else if (result == -4) {
                        this.mStatus = 247;
                    } else if (result == -256) {
                        this.mStatus = 251;
                    } else if (result == -257) {
                        this.mStatus = 242;
                    } else {
                        this.mStatus = result;
                    }
                }

                return this.mStatus;
            }
        }
    }

    private Paint getPaint(int size, boolean bold, boolean fontItalic, String fontName) {
        logfile.printLog(TAG + "===getPaint, fontName:" + fontName + ", mFontNameLast:" + this.mFontNameLast);
        Paint mPaint = new Paint();
        mPaint.reset();
        mPaint.setFlags(Paint.FILTER_BITMAP_FLAG);
        mPaint.setColor(-16777216);
        Typeface typeface = null;
        File file = null;

        try {
            if (TextUtils.equals(fontName, fontName_default)) {
                typeface = Typeface.defaultFromStyle(bold ? Typeface.BOLD : Typeface.NORMAL);
                mPaint.setTypeface(typeface);
                mPaint.setFakeBoldText(bold);
                mPaint.setUnderlineText(false);
                mPaint.setStrikeThruText(false);
                mPaint.setTextSize((float)size);
                return mPaint;
            }

            if (TextUtils.equals(this.mFontNameLast, fontName) && this.mTypefaceLast != null) {
                typeface = this.mTypefaceLast;
            } else {
                typeface = Typeface.create(Typeface.createFromFile(fontName), bold ? Typeface.BOLD : Typeface.NORMAL);
            }
        } catch (Exception var9) {
            Exception e = var9;
            e.printStackTrace();
            logfile.printLog(TAG + "===Exception:" + e.getMessage());
            typeface = Typeface.defaultFromStyle(Typeface.NORMAL);
        }

        this.mTypefaceLast = typeface;
        this.mFontNameLast = fontName;
        mPaint.setTypeface(typeface);
        mPaint.setFakeBoldText(bold);
        mPaint.setUnderlineText(false);
        mPaint.setStrikeThruText(false);
        mPaint.setTextSize((float)size);
        mPaint.setFakeBoldText(bold);
        return mPaint;
    }

    private Paint getPaintCache(int size, boolean bold, boolean fontItalic, String fontName) {
        logfile.printLog(TAG + "===getPaint, fontName:" + fontName + ", mFontNameLast:" + this.mFontNameLast);
        this.mPaint.setTextSize((float)size);
        this.mPaint.setFakeBoldText(bold);
        Typeface typeface = null;

        try {
            if (TextUtils.equals(fontName, fontName_default)) {
                typeface = TypefaceHelper.getDefault(bold, fontName);
            } else {
                typeface = TypefaceHelper.get(bold, fontName);
            }
        } catch (Exception var7) {
            Exception e = var7;
            e.printStackTrace();
            logfile.printLog(TAG + "===Exception:" + e.getMessage());
            typeface = TypefaceHelper.getDefault(bold, fontName);
        }

        this.mPaint.setTypeface(typeface);
        return this.mPaint;
    }

    public String checkFontFileExist(String fontName) {
        logfile.printLog(TAG + "===checkFontFileExist, fontName:" + fontName + ", mFontNameLast:" + this.mFontNameLast);
        File file = null;
        if (TextUtils.isEmpty(fontName)) {
            logfile.printLog(TAG + "===fontName is null, return simsun");
            fontName = fontName_default;
            return fontName;
        } else {
            try {
                file = new File(fontName);
                if (!file.exists()) {
                    fontName = fontName_default;
                }
            } catch (Exception var4) {
                Exception e = var4;
                e.printStackTrace();
                fontName = fontName_default;
            }

            logfile.printLog(TAG + "===checkFontFileExist 2:" + fontName);
            return fontName;
        }
    }

    public Bitmap getQRCodeBitmap(String qrCode, int offset, int height) {
        if (offset > 55) {
            offset -= 55;
        }

        int var10000 = height - offset * 2;
        if (height > 384) {
            height = 384;
        }

        try {
            if (qrCode != null && qrCode.length() != 0) {
                Bitmap qrCodeBmp = EncodingHandler.createQRImage(qrCode, height, height);
                return qrCodeBmp;
            }
        } catch (Exception var6) {
            Exception e = var6;
            e.printStackTrace();
        }

        return null;
    }

    public Bitmap getBarCodeBitmap(String barCode, int width, int height) {
        if (height > 384) {
            height = 384;
        }

        try {
            if (barCode != null && barCode.length() != 0) {
                Bitmap bitmap = EncodingHandler.creatBarcode(barCode, 1, height, false, 1, BarcodeFormat.CODE_128);
                int sourceWidth = bitmap.getWidth();
                int sourceHeight = bitmap.getHeight();
                if (height > 0 && width > 0) {
                    float scaleWidth = (float)width / (float)sourceWidth;
                    float scaleHeight = (float)height / (float)sourceHeight;
                    Matrix matrix = new Matrix();
                    matrix.postScale(scaleWidth, scaleHeight);
                    bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                }

                return bitmap;
            }
        } catch (Exception var10) {
            Exception e = var10;
            e.printStackTrace();
        }

        return null;
    }
}
