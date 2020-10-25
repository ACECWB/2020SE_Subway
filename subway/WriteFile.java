package subway;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;

public class WriteFile {

	public static void write(String text, String saveFile) throws IOException{
		
		StringReader in = new StringReader(text);
		BufferedReader reader = new BufferedReader(in);
		String str;
		if(saveFile.equals("")) { //判断输入的保存路径是否为空， 为空则只打印不保存
			while((str = reader.readLine()) != null) {
				System.out.println(str);
			}
			
		}else {
			FileWriter out = new FileWriter(saveFile, true); // 因为要求查询多个线路信息，因此需要写入方式为追加
			BufferedWriter writer = new BufferedWriter(out);
			
			writer.newLine();
			while((str = reader.readLine()) != null) {
				System.out.println(str);
				
				writer.write(str); //将数据写入缓冲输出流
				writer.newLine(); //输出分隔符

			}
			reader.close();
			writer.close();
			System.out.println("\nYour " + saveFile + " saved successfully!!!\n");
		}
		
	}

}
