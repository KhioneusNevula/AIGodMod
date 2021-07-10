package com.gm910.aigodmod.python;

import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;

import net.minecraft.util.ResourceLocation;

public class PythonUtils {

	public static void execPythonFileFromResourceLocation(Class<?> caller, ResourceLocation loc) {

		ProcessBuilder pb = new ProcessBuilder(
				"C:\\Users\\borah\\AppData\\Local\\Programs\\Python\\Python39\\python.exe",
				"C:\\Users\\borah\\Documents\\GitHub\\AIGodMod\\src\\main\\resources\\assets\\aigodmod\\"
						+ loc.getPath().replace("/", "\\"));
		pb.redirectOutput(Redirect.INHERIT);
		try {
			Process p = pb.start();
		} catch (IOException e) {
			e.printStackTrace();
		}

		/*
		 * try (InputStream resStream = GMUtils.getResourceStream(caller, loc)) { if
		 * (resStream == null) { throw new ReportedException(new
		 * CrashReport("can't access " + loc, new NullPointerException())); } try
		 * (BufferedReader reader = new BufferedReader(new
		 * InputStreamReader(resStream))) {
		 * 
		 * pyInterp.execfile(resStream); } }
		 */

		/*
		 * try (PythonInterpreter pyInterp = new PythonInterpreter()) {
		 * 
		 * pyInterp.setOut(System.out); pyInterp.exec("import sys");
		 * 
		 * pyInterp.exec( "sys.path = ['', " +
		 * "'C:\\\\Users\\\\borah\\\\AppData\\\\Local\\\\Programs\\\\Python\\\\Python39\\\\python39.zip', "
		 * +
		 * "'C:\\\\Users\\\\borah\\\\AppData\\\\Local\\\\Programs\\\\Python\\\\Python39\\\\DLLs', "
		 * +
		 * "'C:\\\\Users\\\\borah\\\\AppData\\\\Local\\\\Programs\\\\Python\\\\Python39\\\\lib', "
		 * +
		 * "'C:\\\\Users\\\\borah\\\\AppData\\\\Local\\\\Programs\\\\Python\\\\Python39', "
		 * +
		 * "'C:\\\\Users\\\\borah\\\\AppData\\\\Local\\\\Programs\\\\Python\\\\Python39\\\\lib\\\\site-packages']"
		 * );
		 * 
		 * pyInterp.exec(
		 * "sys.path.append(\"C:\\\\Users\\\\borah\\\\Documents\\\\GitHub\\\\AIGodMod\\\\run\\\\mods\\\\Lib\")"
		 * ); pyInterp.exec(
		 * "sys.path.append(\"C:\\\\Users\\\\borah\\\\Documents\\\\GitHub\\\\AIGodMod\\\\run\\\\mods\\\\Lib\\\\site-packages\")"
		 * );
		 * 
		 * 
		 * } catch (IOException e) { System.out.println("not found:" + loc);
		 * e.printStackTrace(); }
		 */
	}

}
