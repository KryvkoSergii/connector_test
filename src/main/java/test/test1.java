package test;

import java.util.StringTokenizer;

/**
 * Created by srg on 05.07.16.
 */
public class test1 {

    public static void main(String[] args) {
        String s = "0000005600000003:#InvokeID_1(9;4):0000000f7fffffff00001388001c02960fffffff000003ff00000003000000000000000000000000010c4354494f5353657276657200020c4354494f5353657276657200030c4354494f";
        StringTokenizer st = new StringTokenizer(s, ":");
        String token;
        while (st.hasMoreTokens()) {
            token = st.nextToken();
            if (token.contains("#")) {
                String name = token.substring(1,token.indexOf("("));
                int begin = Integer.valueOf(token.substring(token.indexOf("(")+1,token.indexOf(";")));
                int end = Integer.valueOf(token.substring(token.indexOf(";")+1,token.indexOf(")")));
                System.out.println("name = " + name);
                System.out.println("begin = " + begin);
                System.out.println("end = " + end);
            }
        }
    }
}
