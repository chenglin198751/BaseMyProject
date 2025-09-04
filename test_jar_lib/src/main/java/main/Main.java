package main;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    public static void main(String[] args) {
        EnvUtils.initTools();
        if (args.length == 0) {
            PackTools.Printer.print("请输入参数");
            System.exit(0);
        }

    }


}
