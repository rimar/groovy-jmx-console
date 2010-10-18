
/**
 * @author Yuval Rimar
 */
public class JmxLinksUtil {
    public static String invokeLink(String mbean, String func, String text, String... params) {
        String link = "<a href=\"" + "?" + invoke(func, mbean);
        for (int i = 0; i < params.length; i++) {
            link += "&" + params[i++] + "=" + params[i];
        }
        link += "\">" + text + "</a>";
        return link;
    }

    public static String invoke(String func, String mbean) {
        return "invoke_mbean=Neebula:service=" + mbean + "&invoke_op=" + func;
    }
}
