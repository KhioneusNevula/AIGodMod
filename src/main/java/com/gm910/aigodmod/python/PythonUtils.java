package com.gm910.aigodmod.python;

import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.crash.CrashReport;
import net.minecraft.crash.ReportedException;
import net.minecraft.util.ResourceLocation;

public class PythonUtils {

	/**
	 * TODO generalize this to work with a jar file
	 * 
	 * @param caller
	 * @param loc
	 */
	public static void execPythonFileFromResourceLocation(Class<?> caller, ResourceLocation loc) {

		List<String> pathlist = Arrays.asList(System.getenv("Path").split(";"));
		List<String> pyPaths = pathlist.stream().filter((o) -> o.contains("Python")).collect(Collectors.toList());
		String pyPath = pyPaths.stream().filter((o) -> !o.endsWith("Scripts\\") && !o.endsWith("Scripts/")).findAny()
				.orElse(null);
		if (pyPath == null) {
			throw new ReportedException(CrashReport
					.forThrowable(new IllegalStateException("No python paths found for " + pyPaths), pyPaths + ""));
		}

		String pyExePath = pyPath + "python.exe";

		String runPath = System.getProperty("user.dir").replace("run", "")
				+ "src\\main\\resources\\assets\\aigodmod\\python_ai\\" + loc.getPath().replace("/", "\\");

		System.out.println("Preparing to run python program at " + runPath + " using " + pyExePath);

		ProcessBuilder pb = new ProcessBuilder(pyExePath, runPath);

		pb.redirectOutput(Redirect.INHERIT);
		pb.redirectError(Redirect.INHERIT);
		try {

			Process p = pb.start();
			System.out.println("Running program at " + runPath + " with process " + p);
		} catch (IOException e) {
			throw new ReportedException(
					CrashReport.forThrowable(new IllegalStateException("Cannot run " + loc + " with python"), ""));
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
