package com.gm910.aigodmod.util;

import java.io.IOException;
import java.io.InputStream;

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

	public static CompoundNBT loadNBTFile(InputStream nbtStream) {
		try {
			return CompressedStreamTools.readCompressed(nbtStream);
		} catch (IOException e) {
			throw new ReportedException(CrashReport.forThrowable(e, "Cannot read nbt file from stream "));
		}
	}
}
