package feature_abstract;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class CommandUtil {
    // ������̵���������Ϣ
    private List<String> stdoutList = new ArrayList<String>();
    // ������̵Ĵ�������Ϣ
    private List<String> erroroutList = new ArrayList<String>();

    public void executeCommand(String command) {
        // �����
        stdoutList.clear();
        erroroutList.clear();

        Process p = null;
        try {
            p = Runtime.getRuntime().exec(command);

            // ����2���̣߳��ֱ��ȡ�������������ʹ�����������
            ThreadUtil stdoutUtil = new ThreadUtil(p.getInputStream(), stdoutList);
            ThreadUtil erroroutUtil = new ThreadUtil(p.getErrorStream(), erroroutList);
            //�����̶߳�ȡ����������
            stdoutUtil.start();
            erroroutUtil.start();

            p.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public List<String> getStdoutList() {
        return stdoutList;
    }

    public List<String> getErroroutList() {
        return erroroutList;
    }

}

class ThreadUtil implements Runnable {
    // ���ö�ȡ���ַ�����
    private String character = "GB2312";
    private List<String> list;
    private InputStream inputStream;

    public ThreadUtil(InputStream inputStream, List<String> list) {
        this.inputStream = inputStream;
        this.list = list;
    }

    public void start() {
        Thread thread = new Thread(this);
        thread.setDaemon(true);//��������Ϊ�ػ��߳�
        thread.start();
    }

    public void run() {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(inputStream, character));
            String line = null;
            while ((line = br.readLine()) != null) {
                if (line != null) {
                    list.add(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                //�ͷ���Դ
                inputStream.close();
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
