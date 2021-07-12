package com.gm910.aigodmod.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

import javax.annotation.Nullable;

import net.minecraft.crash.CrashReport;
import net.minecraft.crash.ReportedException;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.util.ResourceLocation;

public class GMUtils {

	public static InputStream getAssetStream(@Nullable Class<?> caller, ResourceLocation loc) {
		return (caller == null ? GMUtils.class : caller).getClassLoader()
				.getResourceAsStream("/assets/" + loc.getNamespace() + "/" + loc.getPath());
	}

	public static InputStream getDataStream(@Nullable Class<?> caller, ResourceLocation loc) {
		return (caller == null ? GMUtils.class : caller).getClassLoader()
				.getResourceAsStream("/data/" + loc.getNamespace() + "/" + loc.getPath());
	}

	public static URL locateDataResource(@Nullable Class<?> caller, ResourceLocation loc) {
		return (caller == null ? GMUtils.class : caller).getClassLoader()
				.getResource("/data/" + loc.getNamespace() + "/" + loc.getPath());
	}

	public static Path locateDataFolder(@Nullable Class<?> caller, ResourceLocation loc)
			throws IOException, URISyntaxException {

		URI uri = (caller == null ? GMUtils.class : caller).getClassLoader()
				.getResource("/data/" + loc.getNamespace() + "/" + loc.getPath()).toURI();
		if ("jar".equals(uri.getScheme())) {
			FileSystem fileSystem = FileSystems.newFileSystem(uri, Collections.emptyMap(), null);
			return fileSystem.getPath("/data/" + loc.getNamespace() + "/" + loc.getPath());
		} else {
			return Paths.get(uri);
		}
	}

	public static CompoundNBT loadNBTFile(InputStream nbtStream) {
		try {
			return CompressedStreamTools.readCompressed(nbtStream);
		} catch (IOException e) {
			throw new ReportedException(CrashReport.forThrowable(e, "Cannot read nbt file from stream "));
		}
	}
}
