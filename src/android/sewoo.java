package com.ezsoft4u.sewoo;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import java.io.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sewoo.port.android.BluetoothPort;
import com.sewoo.request.android.RequestHandler;

import com.sewoo.jpos.command.CPCLConst;
import com.sewoo.jpos.command.ESCPOSConst;
import com.sewoo.jpos.printer.CPCLPrinter;

/**
 * This class echoes a string called from JavaScript.
 */
public class sewoo extends CordovaPlugin {

    private BluetoothPort bluetoothPort;
    private CPCLPrinter cpclPrinter;
    private int paperType;
    private Thread hThread;


    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("Send")) {
            String target = args.getString(0);
            String printcmd = args.getString(1);
            this.Send(target, printcmd, callbackContext);
            return true;
        }
        return false;
    }

   
    private void bluetoothSetup()
    {
        bluetoothPort = BluetoothPort.getInstance();
        cpclPrinter = new CPCLPrinter();
        paperType = CPCLConst.LK_CPCL_CONTINUOUS;
    }
    

    private void Send(String target, String printcmd, CallbackContext callbackContext) {

        RequestHandler rh = new RequestHandler();               
        hThread = new Thread(rh);
        hThread.start();

        bluetoothSetup();

        if (target != null && target.length() > 0) {

            try{
                if(bluetoothPort.isConnected()){
                    
                }else{
                    bluetoothPort.connect(target);
                }
               


                int count = 1;

                cpclPrinter.setForm(0, 200, 200, 670, count);
                cpclPrinter.setMedia(paperType);
                cpclPrinter.setCPCLBarcode(0, 2, 0);
                // Code 39
                cpclPrinter.printCPCLBarcode(CPCLConst.LK_CPCL_0_ROTATION, CPCLConst.LK_CPCL_BCS_39, 2, CPCLConst.LK_CPCL_BCS_1RATIO, 50, 115, 25, "01234567890", 0);
                // Code 93
                cpclPrinter.printCPCLBarcode(CPCLConst.LK_CPCL_0_ROTATION, CPCLConst.LK_CPCL_BCS_93, 2, CPCLConst.LK_CPCL_BCS_1RATIO, 50, 160, 130, "01234567890", 0);
                // Code 128
                cpclPrinter.printCPCLBarcode(CPCLConst.LK_CPCL_0_ROTATION, CPCLConst.LK_CPCL_BCS_128, 2, CPCLConst.LK_CPCL_BCS_1RATIO, 50, 209, 235, "01234567890", 0);
                // PDF 417
                cpclPrinter.printCPCL2DBarCode(0, CPCLConst.LK_CPCL_BCS_PDF417, 200, 320, 3, 7, 2, 1, "SEWOO TECH\r\nLK-P11");
                // QRCODE
                cpclPrinter.printCPCL2DBarCode(0, CPCLConst.LK_CPCL_BCS_QRCODE, 250, 410, 5, 0, 1, 0, "http://www.miniprinter.com");
                // Print
                cpclPrinter.printForm();

                

                hThread.run();
                
                bluetoothPort.disconnect();

                if((hThread != null) && (hThread.isAlive()))
                {
                    hThread.interrupt();
                    hThread = null;
                }   
                
                callbackContext.success("success");    

            }catch(Exception  e){

                hThread.interrupt();
                callbackContext.error("error: " + e);
            }

        } else {
            hThread.interrupt();
            callbackContext.error("fail");
        }
    }

    public String statusCheck()
    {
        String result = "";
        if(!(cpclPrinter.printerCheck() < 0))
        {
            int sts = cpclPrinter.status();
            if(sts == CPCLConst.LK_STS_CPCL_NORMAL)
                return "Normal";
            if((sts & CPCLConst.LK_STS_CPCL_BUSY) > 0)
                result = result + "Busy\r\n";
            if((sts & CPCLConst.LK_STS_CPCL_PAPER_EMPTY) > 0)
                result = result + "Paper empty\r\n";
            if((sts & CPCLConst.LK_STS_CPCL_COVER_OPEN) > 0)
                result = result + "Cover open\r\n";
            if((sts & CPCLConst.LK_STS_CPCL_BATTERY_LOW) > 0)
                result = result + "Battery low\r\n";
        }
        else
        {
            result = "Check the printer\r\nNo response";
        }
        return result;
    }
    

    public void multiLineTest(int count) throws UnsupportedEncodingException
    {
        String data = "ABCDEFGHIJKLMNOPQRSTUVWXYZ;0123456789!@#%^&*\r\n";
        StringBuffer sb = new StringBuffer();
        for(int i=0;i<16;i++)
        {
            sb.append(data);
        }
        
        cpclPrinter.setForm(0, 200, 200, 406, count);
        cpclPrinter.setMedia(paperType);
        
        // MultiLine mode.
        cpclPrinter.setMultiLine(15);
        cpclPrinter.multiLineText(0, 0, 0, 10, 20);
        cpclPrinter.multiLineData(sb.toString());
        cpclPrinter.resetMultiLine();
        // Print
        cpclPrinter.printForm();        
    }
}
