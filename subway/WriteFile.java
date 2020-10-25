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
		if(saveFile.equals("")) { //�ж�����ı���·���Ƿ�Ϊ�գ� Ϊ����ֻ��ӡ������
			while((str = reader.readLine()) != null) {
				System.out.println(str);
			}
			
		}else {
			FileWriter out = new FileWriter(saveFile, true); // ��ΪҪ���ѯ�����·��Ϣ�������Ҫд�뷽ʽΪ׷��
			BufferedWriter writer = new BufferedWriter(out);
			
			writer.newLine();
			while((str = reader.readLine()) != null) {
				System.out.println(str);
				
				writer.write(str); //������д�뻺�������
				writer.newLine(); //����ָ���

			}
			reader.close();
			writer.close();
			System.out.println("\nYour " + saveFile + " saved successfully!!!\n");
		}
		
	}

}
