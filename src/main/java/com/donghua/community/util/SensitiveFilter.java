package com.donghua.community.util;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Component
public class SensitiveFilter {

    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);

    // 替换福
    private static final String REPLACEMENT = "***";

    // 根节点
    private TreeNode rootNode = new TreeNode();

    // 对象实例完成后进行初始化工作
    @PostConstruct
    public void init() {
        try (InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
             BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
        ) {
            String keyword;
            while ((keyword = bufferedReader.readLine()) != null) {
                this.addKeyword(keyword);
            }

        } catch (IOException e) {
            logger.error("加载敏感词文件失败：" + e.getMessage());
        }
    }

    // 将一个敏感词加入字典树中
    private void addKeyword(String keyword) {
        TreeNode tempNode = rootNode;
        for (int i = 0; i < keyword.length(); i++) {
            char c = keyword.charAt(i);
            TreeNode subNode = tempNode.getSubNode(c);

            if (subNode == null) {
                subNode = new TreeNode();
                tempNode.addSubNode(subNode, c);
            }

            tempNode = subNode;

            if (i == keyword.length() - 1){
                tempNode.setKeywordEnd(true);
            }
        }
    }

    /**
     * 过滤敏感词汇
     * @param text  待过滤文本
     * @return  过滤后文本
     */
    public String filter(String text){
        if (StringUtils.isBlank(text)){
            return null;
        }

        // 指针1
        TreeNode tempNode = rootNode;
        // 指针2
        int begin = 0;
        //指针2
        int position = 0;
        // 结果
        StringBuilder sb = new StringBuilder();

        while(position < text.length()){
            char c = text.charAt(position);
            // 跳过符号
            if (isSymbol(c)){
                if (tempNode == rootNode){
                    sb.append(c);
                    begin++;
                }
                position++;
                continue;
            }

            // 检查下级节点
            tempNode = tempNode.getSubNode(c);
            if (tempNode == null){
                // 如果为空，说明没有到达树的尽头，那么begin到position这一块不是敏感词
                sb.append(c);
                position = ++begin;
                tempNode = rootNode;
            }else if (tempNode.isKeywordEnd()){
                // 已经到了树的尽头，说明从begin到position的这一块区域都是敏感词
                sb.append(REPLACEMENT);
                begin = ++position;
                tempNode = rootNode;
            }else {
                position++;
            }

        }
        // 将最后一批字符计入结果
        sb.append(text.substring(begin));
        return sb.toString();

    }

    // 判断是不是特殊字符，防止使用其来规避敏感词
    private boolean isSymbol(Character c){
        // 0x2E80~0x9FFF 属于东亚文字范围
        // 超出了东亚文字范围，并且不属于asc常规字符集，那就是特殊字符
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);
    }


    //前缀树
    private class TreeNode {
        // 关键词结束标志
        private boolean isKeywordEnd = false;

        // 子节点，key是下级字符，value是子节点
        private Map<Character, TreeNode> subnodes = new HashMap<>();


        public boolean isKeywordEnd() {
            return isKeywordEnd;
        }

        public void setKeywordEnd(boolean keywordEnd) {
            isKeywordEnd = keywordEnd;
        }

        // 添加子节点
        public void addSubNode(TreeNode node, Character key) {
            subnodes.put(key, node);
        }

        //获得子节点
        public TreeNode getSubNode(Character key) {
            return subnodes.get(key);
        }
    }
}
