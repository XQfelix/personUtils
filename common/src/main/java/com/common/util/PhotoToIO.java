package com.common.util;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

/**
 * @Title 图片转为IO流/IO流转图片
 * @author GQ_Yin
 */
public class PhotoToIO {
	static BASE64Encoder encoder = new BASE64Encoder();
	static BASE64Decoder decoder = new BASE64Decoder();

	/**
	 * 将图片转换成二进制
	 * @return
	 */
	public static String getImageBinary(String filePath) {
		File f = new File(filePath);
		BufferedImage bi;
		try {
			bi = ImageIO.read(f);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(bi, "jpg", baos);
			byte[] bytes = baos.toByteArray();

			return encoder.encodeBuffer(bytes).trim();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 将二进制转换为图片
	 * @param base64String
	 * @param 图片要保存的位置
	 */
	public static void base64StringToImage(String base64String, String saveFilePath) {
		try {
			byte[] bytes1 = decoder.decodeBuffer(base64String);
			ByteArrayInputStream bais = new ByteArrayInputStream(bytes1);
			BufferedImage bi1 = ImageIO.read(bais);
			File w2 = new File(saveFilePath);// 可以是jpg,png,gif格式
			ImageIO.write(bi1, "jpg", w2);// 不管输出什么格式图片，此处不需改动
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		//String photoToIo = getImageBinary("d:/1.jpg");
		//System.out.println("图片转为io---->>"+photoToIo);
		//base64StringToImage(tt,"d:/qq.jpg");
	}
}
