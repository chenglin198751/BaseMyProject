package main;

public class CommonUtils {
    /**
     * 在数组头部插入一个或多个元素
     */
    public static String[] insertAtHead(String[] array, String... elements) {
        String[] newArray = new String[array.length + elements.length];
        System.arraycopy(elements, 0, newArray, 0, elements.length);
        System.arraycopy(array, 0, newArray, elements.length, array.length);
        return newArray;
    }
}
