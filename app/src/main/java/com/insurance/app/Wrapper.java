package com.insurance.app;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Location;
import android.media.ExifInterface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.provider.ContactsContract.Contacts;
import android.telephony.SmsManager;
import android.util.Log;
import android.util.StringBuilderPrinter;
import android.webkit.WebView;
import android.widget.DatePicker;
import android.widget.TimePicker;
import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Font.FontFamily;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.BaseField;
import com.itextpdf.text.pdf.PdfBoolean;
import com.itextpdf.text.pdf.PdfContentParser;
import com.itextpdf.text.pdf.PdfFormField;
import com.itextpdf.text.pdf.PdfObject;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.collection.PdfCollectionField;
import com.itextpdf.xmp.options.PropertyOptions;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;

import static android.R.attr.data;

public class Wrapper {
    public static String APP_FOLDERNAME = null;
    public static String CB_Success_SMS = null;
    public static String CB_failure_SMS = null;
    public static int REQUEST_CODE_TAKE_PHTO = 0;
    private static final String TAG = "GenerateReport";
    public static WebView appWebView;
    private static Font headFont;
    private static Font imageNameFont;
    public static Activity myActivity;
    private static Font paraHeadFont;
    private static Font paraTextFont;
    private static Font titleFont;
    public String CB_Success;
    public String CB_failure;
    JSONArray incidentArrayMain;
    private boolean isRecording;

    /* renamed from: com.insurance.app.Wrapper.1 */
    class C00231 implements Runnable {
        C00231() {
        }

        public void run() {
            Utility.showActivityIndicator(Wrapper.myActivity, "Loading Photos..", null);
        }
    }

    /* renamed from: com.insurance.app.Wrapper.2 */
    class C00242 extends AsyncTask<Void, Void, Integer> {
        private final /* synthetic */ String val$Category;
        private final /* synthetic */ String val$IncidentId;

        C00242(String str, String str2) {
            this.val$IncidentId = str;
            this.val$Category = str2;
        }

        protected void onPreExecute() {
            super.onPreExecute();
            Utility.showActivityIndicator(Wrapper.myActivity, "Loading Photos..", null);
        }

        protected Integer doInBackground(Void... strings) {
            try {
                JSONArray incidentArray = new JSONArray();
                List<File> files = Utility.getFiles(new File(Wrapper.APP_FOLDERNAME + File.separator + this.val$IncidentId + File.separator + this.val$Category + File.separator).getPath());
                JSONObject incidentObject = new JSONObject();
                if (files != null && files.size() > 0) {
                    System.out.println(files.size());
                    for (File file : files) {
                        incidentObject = new JSONObject();
                        String dateString = Utility.getImageDate(file);
                        Log.e("dateString", file.getName());
                        incidentObject.put("FileName", file.getName());
                        incidentObject.put("CreatedDate", dateString);
                        incidentObject.put("ImageData", file.getAbsolutePath());
                        incidentArray.put(incidentObject);
                    }
                }
                Wrapper.this.incidentArrayMain = incidentArray;
                return Integer.valueOf(1);
            } catch (IOException e) {
                return Integer.valueOf(0);
            } catch (JSONException e2) {
                return Integer.valueOf(0);
            }
        }

        protected void onPostExecute(Integer result) {
            Wrapper.this.invokeSuccessCallback(Wrapper.this.incidentArrayMain.toString());
            Utility.hideActivityIndicator();
        }
    }

    /* renamed from: com.insurance.app.Wrapper.3 */
    class C00253 implements Runnable {
        private final /* synthetic */ String val$webResponseURL;

        C00253(String str) {
            this.val$webResponseURL = str;
        }

        public void run() {
            Wrapper.appWebView.loadUrl(this.val$webResponseURL);
        }
    }

    /* renamed from: com.insurance.app.Wrapper.4 */
    class C00264 extends AsyncTask<Void, Void, Integer> {
        private final /* synthetic */ String val$incidentID;
        private final /* synthetic */ String val$incidentIDStr;

        C00264(String str, String str2) {
            this.val$incidentIDStr = str;
            this.val$incidentID = str2;
        }

        protected void onPreExecute() {
            super.onPreExecute();
            Utility.showActivityIndicator(Wrapper.myActivity, "Creating Report..", null);
        }

        protected Integer doInBackground(Void... strings) {
            try {
                PdfReader.unethicalreading = true;
                File file = new File(Wrapper.APP_FOLDERNAME + File.separator + "template.pdf");
                File reportPDFFile = new File(Wrapper.APP_FOLDERNAME + File.separator + this.val$incidentIDStr + File.separator + "text.pdf");
                if (reportPDFFile.exists()) {
                    reportPDFFile.delete();
                }
                PdfReader reader = new PdfReader(file.getAbsolutePath());
                FileOutputStream os = new FileOutputStream(reportPDFFile.getAbsolutePath(), true);
                PdfStamper stamper = new PdfStamper(reader, os);
                AcroFields form = stamper.getAcroFields();
                for (String key : form.getFields().keySet()) {
                    Log.e("PDF Fields", "Name:" + key);
                }
                String profileDataStr = Wrapper.this.fetchProfileData(PdfObject.NOTHING, PdfObject.NOTHING);
                JSONObject profileJSON = new JSONObject();
                if (profileDataStr != null && profileDataStr.length() > 0) {
                    profileJSON = new JSONObject(profileDataStr);
                }
                Log.e("JSON Data", "Profile:" + profileJSON.toString());
                String incidentDataStr = Wrapper.this.fetchIncident(this.val$incidentIDStr);
                JSONObject incidentJSON = new JSONObject();
                if (incidentDataStr != null && incidentDataStr.length() > 0) {
                    incidentJSON = new JSONObject(incidentDataStr);
                }
                Log.e("JSON Data", "Incident:" + incidentJSON.toString());
                String str = "TxtRecordID";
                form.setField("TxtRecordID", Wrapper.this.FilterData(incidentJSON, "IncidentID"));
                str = "TxtRecordDate";
                form.setField("TxtRecordDate", Wrapper.this.FilterData(incidentJSON, "CreatedDate"));
                str = "TxtCompanyName";
                form.setField("TxtCompanyName", Wrapper.this.FilterData(profileJSON, "InsName"));
                str = "TxtFirstName";
                form.setField("TxtFirstName", Wrapper.this.FilterData(profileJSON, "FirstName"));
                str = "TxtLastName";
                form.setField(str, Wrapper.this.FilterData(profileJSON, "LastName"));
                str = "TxtAddress";
                form.setField(str, Wrapper.this.FilterData(profileJSON, "Address"));
                str = "TxtDOB";
                form.setField(str, Wrapper.this.FilterData(profileJSON, "DOB"));
                str = "TxtGender";
                form.setField(str, Wrapper.this.FilterData(profileJSON, "Gender"));
                str = "TxtAddrCity";
                form.setField(str, Wrapper.this.FilterData(profileJSON, "AddrCity"));
                str = "TxtAddrContactNo";
                form.setField(str, Wrapper.this.FilterData(profileJSON, "ContactNo"));
                str = "TxtLicenceState";
                form.setField(str, Wrapper.this.FilterData(profileJSON, "LicenceState"));
                str = "TxtContactEmail";
                form.setField(str, Wrapper.this.FilterData(profileJSON, "ContactEmail"));
                str = "TxtAddrCountry";
                form.setField(str, Wrapper.this.FilterData(profileJSON, "AddrCountry"));
                str = "TxtAddrPostalCode";
                form.setField(str, Wrapper.this.FilterData(profileJSON, "AddrPostalCode"));
                str = "TxtInsName";
                form.setField(str, Wrapper.this.FilterData(profileJSON, "InsName"));
                str = "TxtLiceneNo";
                form.setField(str, Wrapper.this.FilterData(profileJSON, "LiceneNo"));
                str = "TxtInsPersonalNo";
                form.setField(str, Wrapper.this.FilterData(profileJSON, "InsPersonalNo"));
                str = "TxtLicenceType";
                form.setField(str, Wrapper.this.FilterData(profileJSON, "LicenceType"));
                str = "TxtPropertyType";
                form.setField(str, Wrapper.this.FilterData(profileJSON, "PropertyType"));
                str = "TxtLicenceExpiry";
                form.setField(str, Wrapper.this.FilterData(profileJSON, "LicenceExpiry"));
                str = "TxtVehModel";
                form.setField(str, Wrapper.this.FilterData(profileJSON, "VehModel"));
                str = "TxtPropRegNo";
                form.setField(str, Wrapper.this.FilterData(profileJSON, "PropRegNo"));
                str = "TxtDatePurchased";
                form.setField(str, Wrapper.this.FilterData(profileJSON, "DatePurchased"));
                str = "TxtLicenceCountry";
                form.setField(str, Wrapper.this.FilterData(profileJSON, "LicenceCountry"));
                str = "TxtIsAlarmFitted";
                form.setField(str, Wrapper.this.FilterData(profileJSON, "IsAlarmFitted"));
                str = "TxtIsGPSFitted";
                form.setField(str, Wrapper.this.FilterData(profileJSON, "IsGPSFitted"));
                str = "TxtIsHouseAlarmFitted";
                form.setField(str, Wrapper.this.FilterData(profileJSON, "IsHouseAlarmFitted"));
                str = "TxtLastServiceDate";
                form.setField(str, Wrapper.this.FilterData(profileJSON, "LastServiceDate"));
                str = "TxtGPSNumber";
                form.setField(str, Wrapper.this.FilterData(profileJSON, "GPSNumber"));
                str = "TxtIncidentCategory";
                form.setField(str, Wrapper.this.FilterData(incidentJSON, "IncidentCategory"));
                str = "TxtEnvWeather";
                form.setField(str, Wrapper.this.FilterData(incidentJSON, "EnvWeather"));
                str = "TxtIncidentType";
                form.setField(str, Wrapper.this.FilterData(incidentJSON, "IncidentType"));
                str = "TxtCrashType";
                form.setField(str, Wrapper.this.FilterData(incidentJSON, "CrashType"));
                str = "TxtEnvRoadSurface";
                form.setField(str, Wrapper.this.FilterData(incidentJSON, "EnvRoadSurface"));
                str = "TxtIsThirdPartyDamage";
                form.setField(str, Wrapper.this.FilterData(incidentJSON, "IsThirdPartyDamage"));
                str = "TxtIsPoliceContacted";
                form.setField(str, Wrapper.this.FilterData(incidentJSON, "IsPoliceContacted"));
                str = "TxtWitnessIsAny";
                form.setField(str, Wrapper.this.FilterData(incidentJSON, "WitnessIsAny"));
                str = "TxtIsPoliceInspected";
                form.setField(str, Wrapper.this.FilterData(incidentJSON, "IsPoliceInspected"));
                str = "TxtWitnessName";
                form.setField(str, Wrapper.this.FilterData(incidentJSON, "WitnessName"));
                str = "TxtPoliceReferenceNo";
                form.setField(str, Wrapper.this.FilterData(incidentJSON, "PoliceReferenceNo"));
                str = "TxtIncidentDate";
                form.setField(str, Wrapper.this.FilterData(incidentJSON, "CreatedDate"));
                str = "TxtOtherAccidentDet";
                form.setField(str, Wrapper.this.FilterData(incidentJSON, "OtherAccidentDet"));
                str = "TxtVisibility";
                form.setField(str, Wrapper.this.FilterData(incidentJSON, "Visibility"));
                str = "TxtWeatherTime";
                form.setField(str, Wrapper.this.FilterData(incidentJSON, "WeatherTime"));
                str = "TxtWeatherDate";
                form.setField(str, Wrapper.this.FilterData(incidentJSON, "WeatherDate"));
                form.setField("TxtGPSLatitude", "Latitude:" + Wrapper.this.FilterData(incidentJSON, "GPSLatitude"));
                form.setField("TxtGPSLongitude", "Longitude:" + Wrapper.this.FilterData(incidentJSON, "GPSLongitude"));
                str = "TxtCrashLocation";
                form.setField(str, Wrapper.this.FilterData(incidentJSON, "CrashLocation"));
                stamper.close();
                reader.close();
                os.close();
                step2(this.val$incidentID);
                return Integer.valueOf(1);
            } catch (IOException e) {
                e.printStackTrace();
                return Integer.valueOf(0);
            } catch (JSONException e2) {
                e2.printStackTrace();
                return Integer.valueOf(0);
            } catch (Exception e3) {
                e3.printStackTrace();
                return Integer.valueOf(0);
            }
        }

        public void step2(String incidentId) {
            try {
                File reportPDFFile = new File(Wrapper.APP_FOLDERNAME + File.separator + incidentId + File.separator + "text.pdf");
                File photoPDFFile = new File(Wrapper.APP_FOLDERNAME + File.separator + incidentId + File.separator + "photos.pdf");
                File finalPDFFile = new File(Wrapper.APP_FOLDERNAME + File.separator + incidentId + File.separator + "Report.pdf");
                preparePhotoDocument(incidentId);
                writePhotoIntoTextDocument(reportPDFFile, photoPDFFile, finalPDFFile);
            } catch (Exception e) {
            }
        }

        private void preparePhotoDocument(String incidentId) {
            try {
                FileOutputStream fos = new FileOutputStream(new File(Wrapper.APP_FOLDERNAME + File.separator + incidentId + File.separator + "photos.pdf"));
                Document document = new Document(PageSize.A4);
                PdfWriter.getInstance(document, fos);
                document.open();
                document.newPage();
                writePhotosToPdf("MyPhotos", document);
                writePhotosToPdf(incidentId, document);
                document.close();
            } catch (Exception e) {
            }
        }

        private void writePhotoIntoTextDocument(File reportPDFFile, File photoPDFFile, File finalPDFFile) {
            try {
                Log.e("WRAPPPER REPORT:", "RPT PATH:" + finalPDFFile.getAbsolutePath());
                PdfReader reportReader = new PdfReader(reportPDFFile.getAbsolutePath());
                PdfReader photoReader = new PdfReader(photoPDFFile.getAbsolutePath());
                FileOutputStream os = new FileOutputStream(finalPDFFile.getAbsolutePath());
                PdfStamper stamper = new PdfStamper(reportReader, os);
                Log.d("PAGESSSS", photoReader.getNumberOfPages() + ",Reader PAge:" + reportReader.getNumberOfPages());
                for (int i = 1; i < photoReader.getNumberOfPages() + 1; i++) {
                    PdfTemplate page = stamper.getImportedPage(photoReader, i);
                    stamper.insertPage(reportReader.getNumberOfPages() + i, photoReader.getPageSizeWithRotation(i));
                    stamper.getOverContent(i + 1).addTemplate(page, 0.0f, 0.0f);
                }
                os.flush();
                stamper.close();
                photoReader.close();
                reportReader.close();
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (DocumentException e2) {
                e2.printStackTrace();
            }
        }

        private void writePhotosToPdf(String incidentId, Document document) {
            try {
                List<File> fileListinACategory;
                Paragraph paragraph;
                if (incidentId.equalsIgnoreCase("MyPhotos")) {
                    document.newPage();
                    document.add(new Chunk("PROFILE PHOTOS", Wrapper.headFont));
                    document.add(Chunk.NEWLINE);
                    File profileDir = new File(Wrapper.APP_FOLDERNAME + File.separator + Constants.STR_PROFILE_FOLDER + File.separator + "MyPhotos");
                    if (profileDir.exists()) {
                        fileListinACategory = Utility.getFiles(profileDir.getPath());
                        System.out.println(fileListinACategory.size());
                        if (fileListinACategory == null || fileListinACategory.size() <= 0) {
                            document.newPage();
                            paragraph = new Paragraph(PdfObject.NOTHING);
                            addEmptyLine(paragraph, 5);
                            document.add(paragraph);
                            document.add(new Chunk("               NO PROFILE PHOTOS PRESENT", Wrapper.headFont));
                            return;
                        }
                        writeImageToPdf(fileListinACategory, document);
                        return;
                    }
                    paragraph = new Paragraph(PdfObject.NOTHING);
                    addEmptyLine(paragraph, 5);
                    document.add(paragraph);
                    document.add(new Chunk("               NO PROFILE PHOTOS PRESENT", Wrapper.paraHeadFont));
                    return;
                }
                document.newPage();
                document.add(new Chunk("INCIDENT PHOTOS", Wrapper.headFont));
                List<File> categoryFolders = Utility.getFolders(new File(Wrapper.APP_FOLDERNAME + File.separator + incidentId + File.separator).getPath(), true);
                Log.e("categoryFolders.size()", categoryFolders.size()+"");
                if (categoryFolders == null || categoryFolders.size() <= 0) {
                    paragraph = new Paragraph(PdfObject.NOTHING);
                    addEmptyLine(paragraph, 5);
                    document.add(paragraph);
                    document.add(new Chunk("               NO INCIDENT PHOTOS PRESENT", Wrapper.paraHeadFont));
                    return;
                }
                for (File eachCategoryFolder : categoryFolders) {
                    fileListinACategory = Utility.getFiles(eachCategoryFolder.getPath());
                    Log.e("fileListCat.size()", fileListinACategory.size()+"");
                    if (fileListinACategory != null && fileListinACategory.size() > 0) {
                        writeImageToPdf(fileListinACategory, document);
                        document.newPage();
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (BadElementException e2) {
                e2.printStackTrace();
            } catch (DocumentException e3) {
                e3.printStackTrace();
            }
        }

        public PdfPCell createImageCell(Image img, String title) throws DocumentException, IOException {
            img.setWidthPercentage(100.0f);
            PdfPCell cell = new PdfPCell();
            cell.addElement(img);
            Paragraph p = new Paragraph(title);
            p.setAlignment(1);
            cell.addElement(p);
            cell.setPadding(5.0f);
            return cell;
        }

        private void writeImageToPdf(List<File> fileListinACategory, Document document) {
            if (fileListinACategory != null) {
                try {
                    if (fileListinACategory.size() > 0) {
                        PdfPTable imageTable = new PdfPTable(2);
                        imageTable.setWidthPercentage(98.0f);
                        for (File photo : fileListinACategory) {
                            Log.e("writeImageToPdf", "filePath" + photo);
                            Bitmap bmp = BitmapFactory.decodeStream(new BufferedInputStream(new FileInputStream(photo)));
                            String str = "Orientation";
                            Bitmap bmRotated = rotateBitmap(bmp, new ExifInterface(photo.getPath()).getAttributeInt(ExifInterface.TAG_ORIENTATION,
                                    ExifInterface.ORIENTATION_UNDEFINED));
                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            if (photo.getName().equalsIgnoreCase("myLocation.png")) {
                                bmRotated.compress(CompressFormat.PNG, 50, stream);
                            } else {
                                bmRotated.compress(CompressFormat.JPEG, 50, stream);
                            }
                            Image image = Image.getInstance(stream.toByteArray());
                            Log.e("image.getWidth();", image.getWidth()+"");
                            Log.e("image.getHeight()", image.getHeight()+"");
                            float diff = 1.35f;
                            if (photo.getName().equalsIgnoreCase("myLocation.png")) {
                                diff = BaseField.BORDER_WIDTH_MEDIUM;
                            }
                            float imageWidth = document.getPageSize().getWidth() / diff;
                            float imageHeight = document.getPageSize().getHeight() / BaseField.BORDER_WIDTH_THICK;
                            image.setAlignment(5);
                            String dateString = Utility.getImageDate(photo);
                            if (dateString == null) {
                                dateString = PdfObject.NOTHING;
                            }
                            String str2 = PdfObject.NOTHING;
                            str = ".png";
                            str2 = PdfObject.NOTHING;
                            str = "_";
                            str2 = PdfObject.NOTHING;

                            //File name parsing
                            String fname = photo.getName();
                            fname = fname.replace(".jpg", "");
                            fname = fname.replace(".png", "");
                            fname = fname.replace("_", "");

                            String photoTitle = "Name:(" + fname + "), \n Date:" + dateString;
                            new Paragraph(new Chunk(photoTitle, Wrapper.imageNameFont)).setAlignment(1);
                            imageTable.addCell(createImageCell(image, photoTitle));
                            Log.e("writeImageToPdf", "Done:");
                        }
                        imageTable.completeRow();
                        document.add(imageTable);
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (BadElementException e2) {
                    e2.printStackTrace();
                } catch (MalformedURLException e3) {
                    e3.printStackTrace();
                } catch (IOException e4) {
                    e4.printStackTrace();
                } catch (DocumentException e5) {
                    e5.printStackTrace();
                }
            }
        }

        private Bitmap rotateBitmap(Bitmap bitmap, int orientation) {
            try {
                Matrix matrix = new Matrix();
                switch (orientation) {
                    case PdfWriter.SIGNATURE_APPEND_ONLY /*2*/:
                        matrix.setScale(-1.0f, BaseField.BORDER_WIDTH_THIN);
                        break;
                    case PdfWriter.RUN_DIRECTION_RTL /*3*/:
                        matrix.setRotate(180.0f);
                        break;
                    case PdfWriter.PageLayoutTwoColumnLeft /*4*/:
                        matrix.setRotate(180.0f);
                        matrix.postScale(-1.0f, BaseField.BORDER_WIDTH_THIN);
                        break;
                    case PdfFormField.MK_CAPTION_LEFT /*5*/:
                        matrix.setRotate(90.0f);
                        matrix.postScale(-1.0f, BaseField.BORDER_WIDTH_THIN);
                        break;
                    case PdfFormField.MK_CAPTION_OVERLAID /*6*/:
                        matrix.setRotate(90.0f);
                        break;
                    case PdfCollectionField.SIZE /*7*/:
                        matrix.setRotate(-90.0f);
                        matrix.postScale(-1.0f, BaseField.BORDER_WIDTH_THIN);
                        break;
                    case PdfWriter.PageLayoutTwoColumnRight /*8*/:
                        matrix.setRotate(-90.0f);
                        break;
                    default:
                        return bitmap;
                }
                Bitmap bmRotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                bitmap.recycle();
                return bmRotated;
            } catch (OutOfMemoryError e) {
                e.printStackTrace();
                return null;
            }
        }

        private void addEmptyLine(Paragraph paragraph, int lines) {
            for (int i = 0; i < lines; i++) {
                paragraph.add(new Paragraph(PdfObject.NOTHING));
            }
        }

        protected void onPostExecute(Integer result) {
            Utility.hideActivityIndicator();
            if (result.intValue() == 1) {
                Wrapper.this.invokeSuccessCallback("Done");
            } else {
                Wrapper.this.invokeFailureCallback("Failed");
            }
            File reportPDFFile = new File(Wrapper.APP_FOLDERNAME + File.separator + this.val$incidentID + File.separator + "text.pdf");
            File photoPDFFile = new File(Wrapper.APP_FOLDERNAME + File.separator + this.val$incidentID + File.separator + "photos.pdf");
            if (reportPDFFile.delete() && photoPDFFile.delete()) {
                Log.d("Reports deleted", "Reports deleted");
            }
        }
    }

    /* renamed from: com.insurance.app.Wrapper.5 */
    class C00275 implements OnDateSetListener {
        C00275() {
        }

        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            String dateStr = new StringBuilder(String.valueOf(year)).append("/").append(monthOfYear + 1).append("/").append(dayOfMonth).toString();
            try {
                Wrapper.this.invokeSuccessCallback(new SimpleDateFormat("yyyy/MM/dd").format(new SimpleDateFormat("yyyy/MM/dd").parse(dateStr)));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    /* renamed from: com.insurance.app.Wrapper.6 */
    class C00286 implements OnTimeSetListener {
        C00286() {
        }

        public void onTimeSet(TimePicker view, int selectedHour, int selectedMinute) {
            Log.e("TIME PICKER", "Selected Date:" + new StringBuilder(String.valueOf(selectedHour)).append(":").append(selectedMinute).toString());
            int h = selectedHour;
            String ampm = "AM";
            if (selectedHour > 12) {
                h = selectedHour - 12;
                ampm = " PM";
            } else if (selectedHour == 12) {
                ampm = " PM";
            } else {
                ampm = " AM";
            }
            Wrapper.this.invokeSuccessCallback(new StringBuilder(String.valueOf(h < 10 ? "0" + h : h)).append(":").append(selectedMinute < 10 ? "0" + selectedMinute : selectedMinute).append(ampm).toString());
        }
    }

    static {
        APP_FOLDERNAME = "InsuranceAssist";
        REQUEST_CODE_TAKE_PHTO = PdfContentParser.COMMAND_TYPE;
        titleFont = new Font(FontFamily.TIMES_ROMAN, 20.0f, 1);
        headFont = new Font(FontFamily.TIMES_ROMAN, 20.0f, 1);
        paraHeadFont = new Font(FontFamily.TIMES_ROMAN, 16.0f, 1);
        paraTextFont = new Font(FontFamily.TIMES_ROMAN, 16.0f, 0);
        imageNameFont = new Font(FontFamily.TIMES_ROMAN, 11.0f, 2);
        CB_Success_SMS = "success_callback";
        CB_failure_SMS = "failure_callback";
    }

    public Wrapper(Activity gap, WebView view) {
        this.CB_Success = "success_callback";
        this.CB_failure = "failure_callback";
        this.isRecording = false;
        this.incidentArrayMain = new JSONArray();
        myActivity = gap;
        appWebView = view;
        APP_FOLDERNAME = Utility.getApplicationFolder(myActivity);
    }

    public Wrapper() {
        this.CB_Success = "success_callback";
        this.CB_failure = "failure_callback";
        this.isRecording = false;
        this.incidentArrayMain = new JSONArray();
    }

    public int gotoHome(String requestData, String successCallbackMethodName, String failureCallbackMethodName) {
        invokeURL("javascript:COMMON.success_callback('Test')");
        return 1;
    }

    public String fetchProfileData(String successCB, String failureCB) {
        Log.e("IAWRAPPER", "Reached fetchProfileData Wrapper method");
        String retVal = Constants.TEMPLATE_PROFILE;
        try {
            File profileDir = new File(APP_FOLDERNAME + File.separator + Constants.STR_PROFILE_FOLDER + File.separator + Constants.STR_PROFILE_FILE);
            Log.e("Path", "Path" + profileDir.getAbsolutePath() + ", isExists:" + profileDir.exists());
            if (!profileDir.isFile() || !profileDir.exists()) {
                return retVal;
            }
            FileReader reader = new FileReader(profileDir.getAbsoluteFile());
            BufferedReader bufferedReader = new BufferedReader(reader);
            StringBuffer stringBuffer = new StringBuffer();
            while (true) {
                String line = bufferedReader.readLine();
                if (line == null) {
                    break;
                }
                stringBuffer.append(line).append("\n");
            }
            bufferedReader.close();
            reader.close();
            JSONObject obj = new JSONObject(stringBuffer.toString());
            profileDir = new File(APP_FOLDERNAME + File.separator + Constants.STR_PROFILE_FOLDER + File.separator + "MyPhotos" + File.separator);
            if (profileDir.exists() && profileDir.isDirectory()) {
                obj.put("PhotoCount", profileDir.list().length);
            }
            return obj.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return Constants.TEMPLATE_PROFILE;
        }
    }

    public String saveProfileData(String profileData, String successCB, String failureCB) {
        Log.e("IAWRAPPER", "Reached fetchProfileData Wrapper method");
        String retVal = "Done";
        try {
            File profileDir = new File(APP_FOLDERNAME + File.separator + Constants.STR_PROFILE_FOLDER + File.separator);
            if (!profileDir.exists()) {
                profileDir.mkdirs();
            }
            MediaRefresh(profileDir);
            profileDir = new File(APP_FOLDERNAME + File.separator + Constants.STR_PROFILE_FOLDER + File.separator + Constants.STR_PROFILE_FILE);
            Log.e("Path", "Path" + profileDir.getAbsolutePath() + ", isExists:" + profileDir.exists());
            if (!profileDir.exists()) {
                profileDir.createNewFile();
            }
            if (profileDir.isFile() && profileDir.exists()) {
                profileDir.delete();
                FileWriter fileWritter = new FileWriter(profileDir, false);
                BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
                bufferWritter.write(profileData);
                bufferWritter.close();
                fileWritter.close();
                retVal = "Done";
            }
            MediaRefresh(profileDir);
            return retVal;
        } catch (Exception e) {
            return "ERROR";
        }
    }

    public void showProgress() {
        new Handler().post(new C00231());
    }

    public void hideProgress() {
        Utility.hideActivityIndicator();
    }

    public String fetchPhotos(String pIncidentId, String pCategory, String pPrefix, String successCB, String failureCB) {
        String retVal = PdfObject.NOTHING;
        this.CB_Success = successCB;
        this.CB_failure = failureCB;
        String prefix = pPrefix;
        new C00242(pIncidentId, pCategory).execute(new Void[0]);
        return "Done";
    }

    public String takePhotos(String IncidentId, String Category, String prefix, String successCB, String failureCB) {
        String retVal = PdfObject.NOTHING;
        this.CB_Success = successCB;
        this.CB_failure = failureCB;
        Log.d("IncidentId", IncidentId);
        Log.d("Category", Category);
        Log.d("Prefix", prefix);
        Intent cameraIntent = new Intent(myActivity, CameraActivity.class);
        cameraIntent.putExtra("IncidentId", IncidentId);
        cameraIntent.putExtra("Category", Category);
        cameraIntent.putExtra("Prefix", prefix);
        myActivity.startActivityForResult(cameraIntent, 100);
        Log.e("Wrapper", "Category:" + Category + ", CB:" + successCB);
        return retVal;
    }

    public String deletePhoto(String IncidentId, String Category, String fileName) {
        Log.e("IAWRAPPER", "Reached fetchProfileData Wrapper method");
        try {
            File photoFileDir = new File(APP_FOLDERNAME + File.separator + IncidentId + File.separator + Category + File.separator + fileName);
            Log.e("Path", "Path" + photoFileDir.getAbsolutePath() + ", isExists:" + photoFileDir.exists());
            if (!photoFileDir.isFile() || !photoFileDir.exists()) {
                return "No File exists!";
            }
            photoFileDir.delete();
            return "Done";
        } catch (Exception e) {
            return "Unable to delete the file.!";
        }
    }

    public String fetchHistory(String successCB, String failureCB) {
        String retVal = PdfObject.NOTHING;
        this.CB_Success = successCB;
        this.CB_failure = failureCB;
        try {
            JSONArray incidentArray = new JSONArray();
            List<File> folders = Utility.getIncidentFolders(new File(APP_FOLDERNAME + File.separator).getPath());
            JSONObject incidentObject = new JSONObject();
            Log.e("IAWRAPPER", "Size:" + folders.size());
            if (folders != null && folders.size() > 0) {
                for (File folder : folders) {
                    File dataFile = new File(folder.getAbsoluteFile() + File.separator + "data.json");
                    if (dataFile.exists()) {
                        FileReader reader = new FileReader(dataFile.getAbsoluteFile());
                        BufferedReader bufferedReader = new BufferedReader(reader);
                        StringBuffer stringBuffer = new StringBuffer();
                        while (true) {
                            String line = bufferedReader.readLine();
                            if (line == null) {
                                break;
                            }
                            stringBuffer.append(line).append("\n");
                        }
                        bufferedReader.close();
                        reader.close();
                        incidentObject = new JSONObject(stringBuffer.toString());
                    }
                    incidentArray.put(incidentObject);
                }
            }
            Log.e("IAWRAPPER", "Data:" + incidentArray.toString());
            invokeSuccessCallback(incidentArray.toString());
            return "Done";
        } catch (Exception e) {
            return retVal;
        }
    }

    public String fetchIncident(String incidentID) {
        Log.e("IAWRAPPER", "Reached fetchProfileData Wrapper method");
        String retVal = "{\"IsUploadedToFTP\":\"false\",\"IsUploadedToDropBox\":\"false\",\"GPSLongitude\":\"\",\"IncidentType\":\"Select\",\"IsVoiceRecorded\":\"false\",\"IsEmailed\":\"false\", \"NoOfInjuryPhotos\":0,\"ModifiedDate\":\"\",\"IncidentID\":\"\",\"IncidentCategory\":\"\",\"CrashLocation\":\"\",\"CrashType\":\"\",\"IsPoliceInspected\":\"\",\"NoOfInjured\":\"\",\"EnvWeather\":\"\",\"EnvRoadType\":\"\",\"EnvRoadSurface\":\"\",\"EnvSpeed\":\"\",\"OtherDetails\":\"\",\"IsHouseAlarmFitted\":\"\",\"OtherDamageDetails\":\"\",\"PoliceContactNo\":\"\",\"Visibility\":\"\",\"WeatherTime\":\"\",\"WeatherDate\":\"\",\"ServiceType\":\"\",\"IsThirdPartyDamage\":\"\",\"IsPoliceContacted\":\"\",\"OtherDamage\":\"\",\"OtherAccidentDet\":\"\",\"WitnessIsAny\":\"\",\"WitnessName\":\"\",\"WitnessPhoneNo\":\"\",\"CreatedDate\":\"\",\"PoliceReferenceNo\":\"\",\"NoOfOtherPhotos\":0,\"GPSLatitude\":\"\"}";
        try {
            File incidentDir = new File(APP_FOLDERNAME + File.separator + incidentID + File.separator + "data.json");
            Log.e("Path", "Path" + incidentDir.getAbsolutePath() + ", isExists:" + incidentDir.exists());
            if (!incidentDir.isFile() || !incidentDir.exists()) {
                return retVal;
            }
            FileReader reader = new FileReader(incidentDir.getAbsoluteFile());
            BufferedReader bufferedReader = new BufferedReader(reader);
            StringBuffer stringBuffer = new StringBuffer();
            while (true) {
                String line = bufferedReader.readLine();
                if (line == null) {
                    break;
                }
                stringBuffer.append(line).append("\n");
            }
            bufferedReader.close();
            reader.close();
            JSONObject obj = new JSONObject(stringBuffer.toString());
            incidentDir = new File(APP_FOLDERNAME + File.separator + incidentID + File.separator + "Injury");
            if (incidentDir.isDirectory()) {
                obj.put("NoOfInjuryPhotos", incidentDir.list().length);
            }
            incidentDir = new File(APP_FOLDERNAME + File.separator + incidentID + File.separator + "OtherPhoto");
            if (incidentDir.isDirectory()) {
                obj.put("NoOfOtherPhotos", incidentDir.list().length);
            } else {
                obj.put("NoOfOtherPhotos", 0);
            }
            if (new File(APP_FOLDERNAME + File.separator + incidentID + File.separator + "Report.pdf").exists()) {
                obj.put("IsReportGenerated", PdfBoolean.TRUE);
            } else {
                obj.put("IsReportGenerated", PdfBoolean.FALSE);
            }
            return obj.toString();
        } catch (Exception e) {
            return retVal;
        }
    }

    public String createNewIncident(String incidentID, String createdDate, String incidentType, String categoryType, String RecordType) {
        String retVal = "Done";
        Log.d("createNew incidentType", incidentType);
        Log.d("createNew Record Type", RecordType);
        Log.e("IAWRAPPER", "Reached createNewIncident Wrapper method");
        if (categoryType != null && categoryType.isEmpty()) {
            categoryType = "Select";
        }
        String defaultData = "{\"IsUploadedToFTP\":\"false\",\"IsUploadedToDropBox\":\"false\",\"CrashLocation\":\"\",\"CrashType\":\"\",\"IsPoliceInspected\":\"\",\"NoOfInjured\":\"\",\"EnvWeather\":\"\",\"EnvRoadType\":\"\",\"EnvRoadSurface\":\"\",\"EnvSpeed\":\"\",\"WitnessIsAny\":\"\",\"WitnessName\":\"\",\"WitnessPhoneNo\":\"\",\"IsThirdPartyDamage\":\"\",\"IsPoliceContacted\":\"\",\"OtherDamage\":\"\",\"OtherAccidentDet\":\"\",\"GPSLongitude\":\"\",\"IncidentType\":\"" + incidentType + "\",\"IsVoiceRecorded\":\"false\",\"IsEmailed\":\"false\", " + "\"NoOfInjuryPhotos\":0,\"ModifiedDate\":\"\",\"IncidentID\":\"" + incidentID + "\",\"IncidentCategory\":\"" + categoryType + "\"," + "\"CreatedDate\":\"" + createdDate + "\",\"PoliceReferenceNo\":\"\",\"NoOfOtherPhotos\":0,\"GPSLatitude\":\"\"}";
        try {
            File incidentDir = new File(APP_FOLDERNAME + File.separator + incidentID + File.separator);
            if (incidentDir.exists()) {
                incidentDir.delete();
            }
            incidentDir.mkdirs();
            MediaRefresh(incidentDir);
            JSONObject includeLocation = new JSONObject(defaultData);
            if (!RecordType.equals("EDIT")) {
                Log.e("GPS Location:", "insideFEtch Location:" + RecordType);
                GetLocation objGetLocation = new GetLocation(myActivity);
                if (objGetLocation.displayGpsStatus().booleanValue()) {
                    Location location = objGetLocation.getLocationData();
                    if (location != null)
                    {
                        //Trim the location data
                        String lt = String.valueOf(location.getLatitude());
                        String lng= String.valueOf(location.getLongitude());

                        if(lt.length() >10) {
                            lt = lt.substring(0, 10);
                        }
                        if(lng.length() >10)
                        {
                            lng =lng.substring(0, 10);
                        }

                        includeLocation.put("GPSLatitude", lt);
                        includeLocation.put("GPSLongitude", lng);
                        defaultData = includeLocation.toString();
                        retVal = lt + "," + lng;
                        Log.d("Latitude",lt+"");
                        Log.d("Longitude", lng+"");
                    }
                    else
                    {
                        retVal="NO";
                    }
                } else {
                    retVal = "NO";
                }
            }
            File incidentDataFile = new File(APP_FOLDERNAME + File.separator + incidentID + File.separator + "data.json");
            if (incidentDataFile.isAbsolute() && incidentDataFile.exists()) {
                incidentDataFile.delete();
            }
            incidentDataFile.createNewFile();
            FileWriter fileWritter = new FileWriter(incidentDataFile, false);
            BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
            bufferWritter.write(defaultData);
            bufferWritter.close();
            fileWritter.close();
            MediaRefresh(incidentDataFile);
            return retVal;
        } catch (Exception e) {
            return "ERROR";
        }
    }

    public String saveIncidentData(String incidentID, String incidentData, String successCB, String failureCB) {
        Log.e("IAWRAPPER", "Reached fetchProfileData Wrapper method");
        String retVal = "Done";
        try {
            File profileDir = new File(APP_FOLDERNAME + File.separator + incidentID + File.separator + "data.json");
            Log.e("Path", "Path" + profileDir.getAbsolutePath() + ", isExists:" + profileDir.exists());
            if (profileDir.isFile() && profileDir.exists()) {
                profileDir.delete();
                FileWriter fileWritter = new FileWriter(profileDir, false);
                BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
                bufferWritter.write(incidentData);
                bufferWritter.close();
                fileWritter.close();
                retVal = "Done";
            }
            MediaRefresh(profileDir);
            return retVal;
        } catch (Exception e) {
            return "ERROR";
        }
    }

    public String deleteIncident(String incidentID) {
        String retVal = "DONE";
        try {
            File incidentDir = new File(APP_FOLDERNAME + File.separator + incidentID);
            if (incidentDir.isDirectory() && incidentDir.exists()) {
                deleteDirectory(incidentDir);
                Log.e("WRAPPER", "IsDeleted:done," + incidentDir.getAbsolutePath());
                retVal = "DONE";
            }
            return retVal;
        } catch (Exception e) {
            return "ERROR";
        }
    }

    public static boolean deleteDirectory(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            if (files == null) {
                return true;
            }
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    deleteDirectory(files[i]);
                } else {
                    files[i].delete();
                }
            }
        }
        return path.delete();
    }

    protected void invokeSuccessCallback(Object responseData) {
        try {
            invokeURL("javascript:" + this.CB_Success + "('" + responseData + "')");
        } catch (Exception e) {
        }
    }

    protected void invokeFailureCallback(String errorMessage) {
        try {
            invokeURL("javascript:" + this.CB_failure + "('" + errorMessage + "')");
        } catch (Exception e) {
        }
    }

    /**
     * Used to unvoke the instant WebURL to send detials from Wrapper to Siebel.
     */
    public static void invokeURL(String webUrl) {
        final String webResponseURL = webUrl;
        Log.i("WRAPPER RES", "URL:"+webUrl);
        myActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Log.e("IAWRapper", "INV_URL:" + webResponseURL);
                appWebView.loadUrl(webResponseURL);
            }
        });
    }

    public void ExitApplication() {
        myActivity.finish();
    }

    public String generateUniqueID() {
        String uniqueID = PdfObject.NOTHING;
        return new SimpleDateFormat(Constants.TEMPLATE_UNIQUE_ID).format(new Date());
    }

    public String currentDate() {
        String currentDateStr = PdfObject.NOTHING;
        return new SimpleDateFormat(Constants.TEMPLATE_DATE_FORMAT_TO).format(new Date());
    }

    public void doPreRequites() {
        try {
            String appFolder = Utility.getApplicationFolder(myActivity);
            File profileDir = new File(appFolder);
            if (!profileDir.exists()) {
                profileDir.mkdirs();
            }
            MediaRefresh(profileDir);
            profileDir = new File(new StringBuilder(String.valueOf(appFolder)).append(File.separator).append(Constants.STR_PROFILE_FOLDER).append(File.separator).toString());
            if (!profileDir.exists()) {
                profileDir.mkdirs();
            }
            MediaRefresh(profileDir);
            profileDir = new File(new StringBuilder(String.valueOf(appFolder)).append(File.separator).append(Constants.STR_EMERGENCY_LISTFILE).toString());
            if (!profileDir.exists()) {
                FileWriter fileWritter = new FileWriter(profileDir, false);
                BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
                bufferWritter.write(Constants.TEMPLATE_EMERGENCY_LIST);
                bufferWritter.close();
                fileWritter.close();
                MediaRefresh(profileDir);
            }
            InputStream in = myActivity.getResources().openRawResource(R.raw.template);
            profileDir = new File(new StringBuilder(String.valueOf(appFolder)).append(File.separator).append("template.pdf").toString());
            if (profileDir.exists()) {
                profileDir.delete();
            }
            OutputStream out = new FileOutputStream(profileDir.getAbsoluteFile());
            byte[] buffer = new byte[PdfWriter.PageModeUseOC];
            while (true) {
                int read = in.read(buffer);
                if (read == -1) {
                    in.close();
                    out.flush();
                    out.close();
                    MediaRefresh(profileDir);
                    return;
                }
                out.write(buffer, 0, read);
            }
        } catch (Exception e) {
        }
    }

    public String fetchEmergencyList() {
        String retVal = "{}";
        try {
            File phonelistFile = new File(APP_FOLDERNAME + File.separator + Constants.STR_EMERGENCY_LISTFILE);
            Log.e("Path", "Path" + phonelistFile.getAbsolutePath() + ", isExists:" + phonelistFile.exists());
            if (!phonelistFile.isFile() || !phonelistFile.exists()) {
                return retVal;
            }
            FileReader reader = new FileReader(phonelistFile.getAbsoluteFile());
            BufferedReader bufferedReader = new BufferedReader(reader);
            StringBuffer stringBuffer = new StringBuffer();
            while (true) {
                String line = bufferedReader.readLine();
                if (line == null) {
                    bufferedReader.close();
                    reader.close();
                    return new JSONObject(stringBuffer.toString()).toString();
                }
                stringBuffer.append(line);
            }
        } catch (Exception e) {
            return retVal;
        }
    }

    public String fetchEmergencyListItem(String key) {
        String retVal = "{}";
        try {
            File phonelistFile = new File(APP_FOLDERNAME + File.separator + Constants.STR_EMERGENCY_LISTFILE);
            Log.e("Path", "Path" + phonelistFile.getAbsolutePath() + ", isExists:" + phonelistFile.exists());
            if (!phonelistFile.isFile() || !phonelistFile.exists()) {
                return retVal;
            }
            FileReader reader = new FileReader(phonelistFile.getAbsoluteFile());
            BufferedReader bufferedReader = new BufferedReader(reader);
            StringBuffer stringBuffer = new StringBuffer();
            while (true) {
                String line = bufferedReader.readLine();
                if (line == null) {
                    break;
                }
                stringBuffer.append(line);
            }
            bufferedReader.close();
            reader.close();
            retVal = stringBuffer.toString();
            JSONObject obj = new JSONObject(retVal);
            if (obj.has(key)) {
                return obj.getJSONObject(key).toString();
            }
            return retVal;
        } catch (Exception e) {
            return retVal;
        }
    }

    public String updateEmergencyContact(String type, String name, String contactNumber, String isFavourite, boolean isDelete, String disText, String contEmail) {
        String retVal = "Done";
        try {
            File phonelistFile = new File(APP_FOLDERNAME + File.separator + Constants.STR_EMERGENCY_LISTFILE);
            Log.e("Path", "Path" + phonelistFile.getAbsolutePath() + ", isExists:" + phonelistFile.exists());
            if (!phonelistFile.isFile() || !phonelistFile.exists()) {
                return retVal;
            }
            FileReader reader = new FileReader(phonelistFile.getAbsoluteFile());
            BufferedReader bufferedReader = new BufferedReader(reader);
            StringBuffer stringBuffer = new StringBuffer();
            while (true) {
                String line = bufferedReader.readLine();
                if (line == null) {
                    break;
                }
                stringBuffer.append(line);
            }
            bufferedReader.close();
            reader.close();
            retVal = stringBuffer.toString();
            JSONObject obj = new JSONObject(retVal);
            if (isDelete) {
                obj.remove(type);
            } else {
                JSONObject newContact = new JSONObject();
                newContact.put("Number", contactNumber);
                newContact.put("Name", name);
                newContact.put("IsFavourite", isFavourite);
                newContact.put("ID", disText);
                newContact.put("Email", contEmail);
                if (obj.has(type)) {
                    obj.remove(type);
                }
                obj.put(type, newContact);
            }
            String srcData = obj.toString();
            FileWriter fileWritter = new FileWriter(phonelistFile, false);
            BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
            bufferWritter.write(srcData);
            bufferWritter.close();
            fileWritter.close();
            return retVal;
        } catch (Exception e) {
            return "ERROR";
        }
    }

    private void MediaRefresh(File file) {
        MediaScannerConnection.scanFile(myActivity, new String[]{file.getPath()}, null, null);
    }

    public String ShowReport(String incidentID) {
        String retVal = "Done";
        String pathDest = new StringBuilder(Constants.FILE_PROVIDER_PATH).append(File.separator).append(incidentID).append(File.separator).append("Report.pdf").toString();
        String phyPath = APP_FOLDERNAME + File.separator + incidentID + File.separator + "Report.pdf";
        Log.e("WRAPPER", "Status:false");
        Log.e("WRAPPER", "APP Path:" + APP_FOLDERNAME);
        if (!new File(phyPath).exists()) {
            return "No Report generated.";
        }
        Intent viewIntent = new Intent("android.intent.action.VIEW");
        Log.e("WRAPPER", "APP PATH:" + phyPath);
        viewIntent.setDataAndType(Uri.parse(pathDest), "application/pdf");
        viewIntent.setFlags(PropertyOptions.SEPARATE_NODE);
        myActivity.startActivity(Intent.createChooser(viewIntent, "View Report.."));
        return retVal;
    }

    public void PreviewImage(String filePath) {
        Log.e("WRAPPER", "Image Path:" + filePath);
        Intent previewImgIntent = new Intent(myActivity, ImagePreviewActivity.class);
        previewImgIntent.putExtra("imgFilePath", filePath);
        myActivity.startActivityForResult(previewImgIntent, Constants.REQUEST_CODE_PREVIEW_IMAGE);
    }

    public String EmailReport(String incidentID, String successCB, String failureCB) {
        String retVal = "Done";
        this.CB_Success = successCB;
        this.CB_failure = failureCB;
        try {
            String pathDest = new StringBuilder(Constants.FILE_PROVIDER_PATH).append(File.separator).append(incidentID).append(File.separator).append("Report.pdf").toString();
            File file = new File(APP_FOLDERNAME + File.separator + incidentID + File.separator + "Report.pdf");
            String audioPath = new StringBuilder(Constants.FILE_PROVIDER_PATH).append(File.separator).append(incidentID).append(File.separator).append(incidentID).append(".3gp").toString();
            File audioFile = new File(APP_FOLDERNAME + File.separator + incidentID + File.separator + incidentID + ".3gp");
            ArrayList<Uri> uris = new ArrayList();
            String[] filePaths = new String[]{file.getAbsolutePath(), audioFile.getAbsolutePath()};
            for (String file2 : new String[]{pathDest, audioPath}) {
                Uri fromFile = Uri.fromFile(new File(file2));
                fromFile = Uri.parse(file2);
                uris.add(fromFile);
                Log.e("WRAPPER", "FILE:" + fromFile.toString());
            }
            String profileDataStr = fetchProfileData(PdfObject.NOTHING, PdfObject.NOTHING);
            JSONObject profileJSON = new JSONObject();
            if (profileDataStr != null && profileDataStr.length() > 0) {
                profileJSON = new JSONObject(profileDataStr);
                Log.e("JOY", "ContEmail:" + profileJSON);
            }
            String senderEmail = Constants.INSURANCE_COMP_EMAIL;
            if (profileJSON != null) {
                if (profileJSON.has("ContactEmail")) {
                    Log.e("JOY", "ContEmail:" + profileJSON.getString("ContactEmail"));
                    senderEmail = profileJSON.getString("ContactEmail");
                }
            }
            Intent emailIntent;
            if (file.exists() && audioFile.exists()) {
                emailIntent = new Intent("android.intent.action.SEND_MULTIPLE");
                emailIntent.putExtra("android.intent.extra.EMAIL", new String[]{senderEmail});
                emailIntent.putExtra("android.intent.extra.SUBJECT", "Accident Report for Claim");
                emailIntent.putExtra("android.intent.extra.TEXT", Constants.TEMPLATE_EMAIL_CONTENT);
                emailIntent.setType("text/plain");
                emailIntent.putParcelableArrayListExtra("android.intent.extra.STREAM", uris);
                myActivity.startActivityForResult(Intent.createChooser(emailIntent, "Share report..."), PdfContentParser.COMMAND_TYPE);
                return retVal;
            } else if (!file.exists()) {
                return "No report exists to send email!";
            } else {
                Uri fromFile2 = Uri.fromFile(file);
                fromFile2 = Uri.parse(pathDest);
                emailIntent = new Intent("android.intent.action.SEND");
                emailIntent.putExtra("android.intent.extra.EMAIL", new String[]{senderEmail});
                emailIntent.putExtra("android.intent.extra.SUBJECT", "Accident Report for Claim");
                emailIntent.putExtra("android.intent.extra.TEXT", Constants.TEMPLATE_EMAIL_CONTENT);
                emailIntent.setType("application/pdf");
                emailIntent.putExtra("android.intent.extra.STREAM", fromFile2);
                myActivity.startActivityForResult(Intent.createChooser(emailIntent, "Share report..."), PdfContentParser.COMMAND_TYPE);
                return retVal;
            }
        } catch (Exception e) {
            return "Emailing report failed or No report exists!";
        }
    }

    public String buildAddress(JSONObject profileData) {
        String retVal = PdfObject.NOTHING;
        try {
            if (profileData.has("Address")) {
                if (retVal.length() > 0) {
                    retVal = new StringBuilder(String.valueOf(retVal)).append(",").toString();
                }
                retVal = new StringBuilder(String.valueOf(retVal)).append(profileData.getString("Address")).toString();
            }
            if (profileData.has("AddrSuburb")) {
                if (retVal.length() > 0) {
                    retVal = new StringBuilder(String.valueOf(retVal)).append(",").toString();
                }
                retVal = new StringBuilder(String.valueOf(retVal)).append(profileData.getString("AddrSuburb")).toString();
            }
            if (profileData.has("AddrCity")) {
                if (retVal.length() > 0) {
                    retVal = new StringBuilder(String.valueOf(retVal)).append(",").toString();
                }
                retVal = new StringBuilder(String.valueOf(retVal)).append(profileData.getString("AddrCity")).toString();
            }
            if (profileData.has("AddrCountry")) {
                if (retVal.length() > 0) {
                    retVal = new StringBuilder(String.valueOf(retVal)).append(",").toString();
                }
                retVal = new StringBuilder(String.valueOf(retVal)).append(profileData.getString("AddrCountry")).toString();
            }
            if (profileData.has("AddrPostalCode")) {
                if (retVal.length() > 0) {
                    retVal = new StringBuilder(String.valueOf(retVal)).append(",").toString();
                }
                retVal = new StringBuilder(String.valueOf(retVal)).append(profileData.getString("AddrPostalCode")).toString();
            }
        } catch (Exception e) {
        }
        return retVal;
    }

    public String FilterData(JSONObject jsonObj, String Key) {
        String retVal = PdfObject.NOTHING;
        try {
            Log.e("FILTER DATA", "Key:" + Key + ", VAL:" + jsonObj.getString(Key).toUpperCase());
            Log.e("FILTER DATA", "is VALid:" + (!jsonObj.getString(Key).equalsIgnoreCase("SELECT")));
            if (!jsonObj.has(Key) || jsonObj.getString(Key).equalsIgnoreCase("SELECT")) {
                return retVal;
            }
            return jsonObj.getString(Key);
        } catch (Exception e) {
            return PdfObject.NOTHING;
        }
    }

    public String GenerateReport(String incidentID, String successCB, String failureCB) {
        Log.e("WRAPPER", "Request Reached Wrapper...");
        Log.e("WRAPPER", "Incident ID..." + incidentID);
        String retVal = "Done";
        this.CB_Success = successCB;
        this.CB_failure = failureCB;
        new C00264(incidentID, incidentID).execute(new Void[0]);
        return retVal;
    }

    public String recordAudio(String incidentID, String successCB, String failureCB) {
        Log.e("WRAPPER", "Request Reached Wrapper- recordAudio");
        String retVal = "Done";
        this.CB_Success = successCB;
        this.CB_failure = failureCB;
        try {
            if (this.isRecording) {
                AudioRecord.stopRecording();
                this.isRecording = false;
                retVal = "Recording Stopped";
            } else {
                AudioRecord.startRecording(incidentID);
                this.isRecording = true;
                retVal = "Recording Started";
            }
        } catch (Exception e) {
            e.printStackTrace();
            retVal = "ERROR";
            invokeFailureCallback(retVal);
        }
        invokeSuccessCallback(retVal);
        return retVal;
    }

    public String getLocation(String incidentID, String successCB, String failureCB) {
        Log.e("WRAPPER", "Request Reached Wrapper- getLocation");
        String retVal = "Done";
        this.CB_Success = successCB;
        this.CB_failure = failureCB;
        try {
            File incidentDir = new File(APP_FOLDERNAME + File.separator + incidentID + File.separator + "data.json");
            Log.e("Path", "Path" + incidentDir.getAbsolutePath() + ", isExists:" + incidentDir.exists());
            if (!incidentDir.isFile() || !incidentDir.exists()) {
                return retVal;
            }
            FileReader reader = new FileReader(incidentDir.getAbsoluteFile());
            BufferedReader bufferedReader = new BufferedReader(reader);
            StringBuffer stringBuffer = new StringBuffer();
            while (true) {
                String line = bufferedReader.readLine();
                if (line == null) {
                    break;
                }
                stringBuffer.append(line).append("\n");
            }
            bufferedReader.close();
            reader.close();
            String defaultData = stringBuffer.toString();
            JSONObject obj = new JSONObject(defaultData);
            Log.d("GEOLOCATION", "Before Geo-location Fetch:");
            GetLocation objGetLocation = new GetLocation(myActivity);
            if (objGetLocation.displayGpsStatus().booleanValue()) {
                Location location = objGetLocation.getLocationData();
                if (location != null) {
                    obj.put("GPSLatitude", location.getLatitude());
                    obj.put("GPSLongitude", location.getLongitude());
                    defaultData = obj.toString();
                    retVal = location.getLatitude() + "," + location.getLongitude();
                    Log.d("Latitude", location.getLatitude()+"");
                    Log.d("Longitude", location.getLongitude()+"");
                }
                else
                {
                    retVal="NO";
                }
            } else {
                retVal = "OFF";
            }
            File incidentDataFile = new File(APP_FOLDERNAME + File.separator + incidentID + File.separator + "data.json");
            if (incidentDataFile.isAbsolute() && incidentDataFile.exists()) {
                incidentDataFile.delete();
            }
            incidentDataFile.createNewFile();
            FileWriter fileWritter = new FileWriter(incidentDataFile, false);
            BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
            bufferWritter.write(defaultData);
            bufferWritter.close();
            fileWritter.close();
            invokeSuccessCallback(retVal);
            return retVal;
        } catch (Exception e) {
            e.printStackTrace();
            retVal = "ERROR";
            invokeFailureCallback(retVal);
            return retVal;
        }
    }

    public void showLocation(String latitude, String longitude) {
    }

    public String getLocationData() {
        JSONObject data = new JSONObject();
        try {
            GetLocation objGetLocation = new GetLocation(myActivity);
            if (!objGetLocation.displayGpsStatus().booleanValue()) {
                return "OFF";
            }
            Location location = objGetLocation.getLocationData();
            if (location == null) {
                return null;
            }
            data.put("GPSLatitude", location.getLatitude());
            data.put("GPSLongitude", location.getLongitude());
            return data.toString();
        } catch (Exception e) {
            return null;
        }
    }

    public int pickDate(String requestData, String successCB, String failureCB) {
        this.CB_Success = successCB;
        this.CB_failure = failureCB;
        try {
            int mDay;
            JSONObject datePickerReq = new JSONObject(requestData);
            String reqKey = "Request";
            String Title = datePickerReq.getJSONObject(reqKey).getString("Title");
            String DefaultDay = datePickerReq.getJSONObject(reqKey).getString("DefaultDay");
            String DefaultMonth = datePickerReq.getJSONObject(reqKey).getString("DefaultMonth");
            String DefaultYear = datePickerReq.getJSONObject(reqKey).getString("DefaultYear");
            Calendar c = Calendar.getInstance();
            int mYear = DefaultYear != null ? Integer.parseInt(DefaultYear) : c.get(Calendar.YEAR);
            int mMonth = DefaultMonth != null ? Integer.parseInt(DefaultMonth) : c.get(Calendar.MONTH);
            if (DefaultDay != null) {
                mDay = Integer.parseInt(DefaultDay);
            } else {
                mDay = c.get(Calendar.DAY_OF_MONTH);
            }
            int month = 0;
            if (mMonth - 1 > 0) {
                month = mMonth - 1;
            }
            DatePickerDialog dpd = new DatePickerDialog(myActivity, new C00275(), mYear, month, mDay);
            if (Title == null || Title.length() <= 0) {
                dpd.setTitle("Select Date");
            } else {
                dpd.setTitle(Title);
            }
            dpd.show();
            return 1;
        } catch (Exception e) {
            e.printStackTrace();
            return 1;
        }
    }

    public int pickTime(String requestData, String successCB, String failureCB) {
        this.CB_Success = successCB;
        this.CB_failure = failureCB;
        try {
            int mHour2;
            JSONObject datePickerReq = new JSONObject(requestData);
            String reqKey = "Request";
            String Title = datePickerReq.getJSONObject(reqKey).getString("Title");
            String DefaultHour = datePickerReq.getJSONObject(reqKey).getString("DefaultHour");
            String DefaultMin = datePickerReq.getJSONObject(reqKey).getString("DefaultMin");
            String DefaultAMPM = datePickerReq.getJSONObject(reqKey).getString("DefaultAMPM");
            Calendar c = Calendar.getInstance();
            int mHour = DefaultHour != null ? Integer.parseInt(DefaultHour) : c.get(Calendar.HOUR);
            int mMin = DefaultMin != null ? Integer.parseInt(DefaultMin) : c.get(Calendar.MINUTE);
            if (DefaultAMPM.equalsIgnoreCase("PM")) {
                mHour2 = mHour + 12;
            } else {
                mHour2 = mHour;
            }
            TimePickerDialog timepick = new TimePickerDialog(myActivity, new C00286(), mHour2, mMin, false);
            if (Title == null || Title.length() <= 0) {
                timepick.setTitle("Select Time");
            } else {
                timepick.setTitle(Title);
            }
            timepick.show();
            return 1;
        } catch (Exception e) {
            e.printStackTrace();
            return 1;
        }
    }

    public int TrackLocaiton(String mobileNumber, String successCB, String failureCB) {
        CB_Success_SMS = successCB;
        CB_failure_SMS = failureCB;
        try {
            SmsManager.getDefault().sendTextMessage(mobileNumber, null, "GETLOCHASH", null, null);
            InvokeSMSResponse("Waiting..");
        } catch (Exception e) {
        }
        return 1;
    }

    public static void InvokeSMSResponse(String msg) {
        try {
            Log.e("WRAPPER", "URL" + CB_Success_SMS);
            invokeURL("javascript:" + CB_Success_SMS + "('" + msg + "')");
        } catch (Exception e) {
        }
    }

    public String getSecurityPin() {
        String pinNo = PdfObject.NOTHING;
        try {
            return myActivity.getSharedPreferences("SETTINGS_PREFERENCES", 0).getString("PIN", PdfObject.NOTHING);
        } catch (Exception e) {
            return PdfObject.NOTHING;
        }
    }

    public String updateSecurityPin(String pinFromJS) {
        String retVal = "DONE";
        try {
            Editor editor = myActivity.getSharedPreferences("SETTINGS_PREFERENCES", 0).edit();
            editor.putString("PIN", pinFromJS);
            editor.commit();
            Log.e("WRAPPER", "Update Status: done");
            return retVal;
        } catch (Exception e) {
            return "ERROR";
        }
    }

    public void ShowMap(String latitude, String longitude, String incidentID) {
        try {
            Intent showMapIntent = new Intent(myActivity, ShowMap.class);
            showMapIntent.putExtra("SHOWLAT", latitude);
            showMapIntent.putExtra("SHOWLONG", longitude);
            showMapIntent.putExtra("INCIDENTID", incidentID);
            myActivity.startActivityForResult(showMapIntent, Constants.REQUEST_CODE_GET_LOCATION);
        } catch (Exception e) {
        }
    }

    public void pickContacts(String requestData, String successCB, String failureCB) {
        Log.e("IAWRAPPER", "Reached pickContacts Wrapper method");
        try {
            this.CB_Success = successCB;
            this.CB_failure = failureCB;
            Log.e("WRAPPER INBOUND", "CB Methods:" + CB_Success_SMS + "," + CB_failure_SMS);
            myActivity.startActivityForResult(new Intent("android.intent.action.PICK", Contacts.CONTENT_URI), Constants.REQUEST_CODE_PICK_CONTACT);
        } catch (Exception e) {
        }
    }

    /*
    This method will help to get emergency contact details for Nearest Hospital, Nearest Police Station and Fire Station
     */
    public boolean GetEmergencyContactDetails(String latitude, String longitude,  String successCB, String failureCB){
        this.CB_Success = successCB;
        this.CB_failure = failureCB;
        (new EmergencyContactTask(latitude, longitude)).execute();
        return true;
    }

    //Async Task for Fetching Emergency Contact details
    class EmergencyContactTask extends AsyncTask<String, String, Boolean> {
        private String latitude;
        private String longitude;
        private StringBuilder sbOutput = new StringBuilder();

        EmergencyContactTask(String lat, String lang) {
            this.latitude = lat;
            this.longitude = lang;
        }

        protected void onPreExecute() {
            super.onPreExecute();
            Utility.showActivityIndicator(Wrapper.myActivity, "Searching..", null);
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            BufferedReader reader = null;
            try {
                String urlStr = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=-33.8670522,151.1957362&radius=500&type=restaurant&keyword=cruise&key=AIzaSyAd7H1BlaIwjvNQYwTzGd9AhWPHpKztM8g";
                URL url = new URL(urlStr);

                trustEveryone();

                URLConnection conn = url.openConnection();
                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);
                conn.addRequestProperty("Content-Type", "application/json");
                //conn.connect();
                InputStream stream = conn.getInputStream();
                InputStreamReader streamreader = new InputStreamReader(stream);
                reader = new BufferedReader(streamreader);
                String line = null;

                while((line = reader.readLine()) != null)
                {
                    // Append server response in string
                    sbOutput.append(line + "");
                }
                Log.i("JOY", "Content Success." + sbOutput.toString().length());
            } catch (Exception exp) {
                Log.i("JOY", "ERROR:" + exp.getMessage());
                Wrapper.this.invokeFailureCallback("Failed Searching Contacts");
                return false;
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (Exception ex) {
                    }
                }
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if(aBoolean) {
                Wrapper.this.invokeSuccessCallback("Success");// + sbOutput.toString());
                Log.i("WRAPPER", "On Post Execute");
            }
            Utility.hideActivityIndicator();
        }
    }

    private void trustEveryone() {
        try {
            Log.i("JOY", "Trusting All host..");
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier(){
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }});
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, new X509TrustManager[]{new X509TrustManager(){
                public void checkClientTrusted(X509Certificate[] chain,
                                               String authType) throws CertificateException {}
                public void checkServerTrusted(X509Certificate[] chain,
                                               String authType) throws CertificateException {}
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }}}, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(
                    context.getSocketFactory());

            Log.i("JOY", "Host Trusted All");
        } catch (Exception e) { // should never happen
            e.printStackTrace();
        }
    }
}
