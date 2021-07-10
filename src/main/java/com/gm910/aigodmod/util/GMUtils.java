package com.gm910.aigodmod.util;

import java.io.InputStream;

import javax.annotation.Nullable;

import net.minecraft.util.ResourceLocation;

public class GMUtils {

	public static InputStream getResourceStream(@Nullable Class<?> caller, ResourceLocation loc) {
		return (caller == null ? GMUtils.class : caller).getClassLoader().getResourceAsStream("/assets/"+loc.getNamespace() + "/"+loc.getPath());
	}
}
