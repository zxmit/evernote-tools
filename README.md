# evernote-tools
> 诞生

在电脑上经常使用Stickies做每日记录，时间久了就在桌面上挤压大量便签，相当之不美观，当然不能随随便便的删掉，索性保存在印象笔记中，作为一个极其懒的人，又不想每次保存都打开印象笔记，因此这个工具就诞生了。

> 规划

便签一般停留在桌面一周，然后直接保存在某个目录中，写一个小工具定时监控该目录，并将便签同步到印象笔记中

> 参考资料

[讲的很详细了，基本能够找到我需要的功能](http://www.jianshu.com/p/62dd29d8a684)  
[官方文档](https://dev.yinxiang.com/doc/)

> 注意事项

 A. 便签保存后默认编码为UTF-16LE，在读取文本时需要进行转义：
 
 ```
 fin = new FileInputStream(path);  
 in = new InputStreamReader(fin, "UTF-16");  
 br = new BufferedReader(in);
 ```
 
 注： 可通过UE编辑器查看其编码格式
 
 B. 在构建笔记内容时，注意特殊字符的处理，最好使用`<![CDATA[]]>`将内容包裹起来：
 
 ```
 public String addStyle(List<String> content) {
 	String body = "";  
 	for(String str : content) {
 		body += "<div style=\"font-weight:bold;color:blue;\"><![CDATA["+str+"]]></div>";
 	}
 	return body;
 }
 ```
 




