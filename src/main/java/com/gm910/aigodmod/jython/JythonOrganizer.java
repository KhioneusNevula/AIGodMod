package com.gm910.aigodmod.jython;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.python.core.PyObject;
import org.python.util.PythonInterpreter;

public class JythonOrganizer {

	public static PyObject runFromFile(String filePath) {
		Path path = Paths.get(filePath);
		String codeString = "";
		
		try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.US_ASCII)) {
			String line = null;
			while ((line = reader.readLine()) != null) {
				codeString += line + "\n";
			}
			codeString = codeString.trim();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return run(codeString);
		
	}
	
	public static PyObject run(String code) {
		try(PythonInterpreter pyInterp = new PythonInterpreter()) {
			
			return pyInterp.eval(code);
		}
	}
}
