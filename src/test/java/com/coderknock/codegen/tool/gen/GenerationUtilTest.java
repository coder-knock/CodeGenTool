package com.coderknock.codegen.tool.gen;

import com.coderknock.codegen.tool.domin.Result;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 单元测试 for GenerationUtil.enumIsXXX 方法
 * 覆盖场景：
 * 1. 正常枚举带属性 - 验证生成正确方法
 * 2. 枚举没有属性 - 验证不会 NPE（修复 bug 1）
 * 3. 验证只生成一个 isExist 方法（修复 bug 2）
 * 4. 空枚举 - 没有枚举常量
 * 5. 带 @EqualsField 注解的枚举
 * 6. 原生类型字段的枚举
 * 7. 非枚举输入 - 返回错误
 */
class GenerationUtilTest {

    @Test
    void testEnumWithFields() {
        // 测试场景：正常枚举，带有一个属性
        String javaCode = """
                package com.coderknock.codegen.tool.test;

                public enum Color {
                    RED("#FF0000"),
                    GREEN("#00FF00"),
                    BLUE("#0000FF"),
                    ;

                    private final String code;

                    Color(String code) {
                        this.code = code;
                    }

                    public String getCode() {
                        return code;
                    }
                }
                """;

        Result<String> result = GenerationUtil.enumIsXXX(javaCode);

        // 验证成功
        assertTrue(result.isSuccess());
        String generatedCode = result.getData();

        // 验证生成了每个枚举常量的 isXXX 方法（基于实例比较）
        assertTrue(generatedCode.contains("public static boolean isRed(Color color)"));
        assertTrue(generatedCode.contains("public static boolean isGreen(Color color)"));
        assertTrue(generatedCode.contains("public static boolean isBlue(Color color)"));

        // 验证生成了每个枚举常量的 isXXX 方法（基于字段比较）
        assertTrue(generatedCode.contains("public static boolean isRed(String code)"));
        assertTrue(generatedCode.contains("public static boolean isGreen(String code)"));
        assertTrue(generatedCode.contains("public static boolean isBlue(String code)"));

        // 验证只生成一个 isExist 方法（bug 2 修复验证）
        long isExistCount = countOccurrences(generatedCode, "public static boolean isExist");
        assertEquals(2, isExistCount); // 一个是 (Color color)，一个是 (String code)

        // 验证导入 Arrays
        assertTrue(generatedCode.contains("import java.util.Arrays;"));
    }

    @Test
    void testEnumWithoutNonStaticFields() {
        // 测试场景：枚举没有属性（修复 bug 1：NPE 问题）
        // 之前这里 enumConstantFullQuote 未初始化会导致 NPE
        String javaCode = """
                package com.coderknock.codegen.tool.test;

                public enum SimpleEnum {
                    FIRST,
                    SECOND,
                    THIRD;
                }
                """;

        Result<String> result = GenerationUtil.enumIsXXX(javaCode);

        // 验证成功 - 不应该抛出异常
        assertTrue(result.isSuccess());
        String generatedCode = result.getData();

        // 验证生成了每个枚举常量的 isXXX 方法
        assertTrue(generatedCode.contains("public static boolean isFirst(SimpleEnum simpleEnum)"));
        assertTrue(generatedCode.contains("public static boolean isSecond(SimpleEnum simpleEnum)"));
        assertTrue(generatedCode.contains("public static boolean isThird(SimpleEnum simpleEnum)"));

        // 验证只生成一个 isExist 方法（bug 2 修复验证）
        long isExistCount = countOccurrences(generatedCode, "public static boolean isExist");
        assertEquals(1, isExistCount); // 只有一个 (SimpleEnum simpleEnum)

        // 验证没有基于字段的 isExist 方法（因为没有字段）
        assertFalse(generatedCode.contains("isExist(") && generatedCode.contains(")") && !generatedCode.contains("SimpleEnum"));
    }

    @Test
    void testVerifyOnlyOneIsExistGenerated() {
        // 专门验证 bug 2：修复前会为每个枚举常量生成一个 isExist，导致多个重复方法
        String javaCode = """
                package com.coderknock.codegen.tool.test;

                public enum Season {
                    SPRING,
                    SUMMER,
                    AUTUMN,
                    WINTER;
                }
                """;

        Result<String> result = GenerationUtil.enumIsXXX(javaCode);
        assertTrue(result.isSuccess());
        String generatedCode = result.getData();

        // 修复后：只能有一个 isExist 方法
        long isExistCount = countOccurrences(generatedCode, "public static boolean isExist");
        assertEquals(1, isExistCount, "应该只生成一个 isExist 方法，修复前会生成 " +
            "4 个（每个枚举常量一个）");
    }

    @Test
    void testEmptyEnum() {
        // 测试场景：没有枚举常量
        String javaCode = """
                package com.coderknock.codegen.tool.test;

                public enum EmptyEnum {
                }
                """;

        Result<String> result = GenerationUtil.enumIsXXX(javaCode);
        assertTrue(result.isSuccess());
        // 应该返回原代码，不添加任何方法
        assertEquals("package com.coderknock.codegen.tool.test;%n%npublic enum EmptyEnum {%n}", result.getData().trim());
    }

    @Test
    void testEnumWithEqualsFieldAnnotation() {
        // 测试场景：带有 @EqualsField 注解指定特定字段
        String javaCode = """
                package com.coderknock.codegen.tool.test;

                public enum Status {
                    ACTIVE(1, "激活"),
                    INACTIVE(0, "未激活"),
                    DELETED(2, "已删除"),
                    ;

                    @EqualsField
                    private final int code;
                    private final String desc;

                    Status(int code, String desc) {
                        this.code = code;
                        this.desc = desc;
                    }

                    public int getCode() {
                        return code;
                    }

                    public String getDesc() {
                        return desc;
                    }
                }
                """;

        Result<String> result = GenerationUtil.enumIsXXX(javaCode);
        assertTrue(result.isSuccess());
        String generatedCode = result.getData();

        // 验证使用 code 字段生成基于字段的比较方法
        assertTrue(generatedCode.contains("public static boolean isActive(int code)"));
        // 对于原生类型应该使用 == 而不是 equals
        assertTrue(generatedCode.contains("streamValue.code == code"));

        // 验证只生成一个 isExist
        long isExistCount = countOccurrences(generatedCode, "public static boolean isExist");
        assertEquals(2, isExistCount);
    }

    @Test
    void testEnumWithPrimitiveField() {
        // 测试场景：原生类型字段，验证正确使用 == 而不是 equals
        String javaCode = """
                package com.coderknock.codegen.tool.test;

                public enum HttpStatus {
                    OK(200),
                    NOT_FOUND(404),
                    SERVER_ERROR(500),
                    ;

                    private final int code;

                    HttpStatus(int code) {
                        this.code = code;
                    }
                }
                """;

        Result<String> result = GenerationUtil.enumIsXXX(javaCode);
        assertTrue(result.isSuccess());
        String generatedCode = result.getData();

        // 验证原生类型使用 ==
        assertTrue(generatedCode.contains("streamValue.code == code"));
        assertFalse(generatedCode.contains("streamValue.code.equals"));
    }

    @Test
    void testNotEnum() {
        // 测试场景：输入不是枚举
        String javaCode = """
                package com.coderknock.codegen.tool.test;

                public class NormalClass {
                    private String name;
                }
                """;

        Result<String> result = GenerationUtil.enumIsXXX(javaCode);

        // 验证返回失败
        assertFalse(result.isSuccess());
        assertEquals(400, result.getCode());
    }

    @Test
    void testEnumWithMultipleFieldsUsesFirst() {
        // 测试场景：多个字段没有 @EqualsField 应该使用第一个非静态字段
        String javaCode = """
                package com.coderknock.codegen.tool.test;

                public enum Size {
                    S("Small", 1),
                    M("Medium", 2),
                    L("Large", 3),
                    ;

                    private final String name;
                    private final int code;

                    Size(String name, int code) {
                        this.name = name;
                        this.code = code;
                    }
                }
                """;

        Result<String> result = GenerationUtil.enumIsXXX(javaCode);
        assertTrue(result.isSuccess());
        String generatedCode = result.getData();

        // 验证使用第一个字段 name (String 类型)
        assertTrue(generatedCode.contains("public static boolean isS(String name)"));
        assertTrue(generatedCode.contains("streamValue.name.equals(name)"));
    }

    @Test
    void testIdempotentGeneration() {
        // 测试：多次调用应该不会重复生成方法
        String javaCode = """
                package com.coderknock.codegen.tool.test;

                public enum Direction {
                    NORTH,
                    SOUTH,
                    EAST,
                    WEST;
                }
                """;

        // 第一次生成
        Result<String> result1 = GenerationUtil.enumIsXXX(javaCode);
        assertTrue(result1.isSuccess());
        String firstGeneration = result1.getData();

        // 用生成后的代码再次生成
        Result<String> result2 = GenerationUtil.enumIsXXX(firstGeneration);
        assertTrue(result2.isSuccess());
        String secondGeneration = result2.getData();

        // 验证 isExist 仍然只有一个
        long isExistCount = countOccurrences(secondGeneration, "public static boolean isExist");
        assertEquals(1, isExistCount);

        // 验证 isNorth 等方法也只有一个
        long isNorthCount = countOccurrences(secondGeneration, "public static boolean isNorth");
        assertEquals(1, isNorthCount);
    }

    /**
     * 统计字符串中出现次数
     */
    private long countOccurrences(String text, String substring) {
        return text.lines()
            .filter(line -> line.contains(substring))
            .count();
    }
}
