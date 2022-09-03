# compiler
compiler in java  
Syntax Analysis  
基于Java语言实现龙书的代码，一步一步实现一个语法分析器，语法制导的翻译，中间代码的生成

# 对象
- 项
   - LR(0)item
   - LR(1)item
- 项集
   - LR(0)项集
   - LR(1)项集
- 符号
   - 终结符
   - 非终结符
- 文法

# 算法
The construction of both top-down and bottom-up parsers is aided by tow functions FIRST and FOLLOW, associated with a grammar G
- FIRST
- FOLLOW
- collection of sets of LR(0)items
- closure LR(1)items
- LALR(1)项集族的内核的高效计算方法
- 高效构造LALR语法分析表 Efficient Construction of LALR Parsing Tables
- 打印语法分析表 print Parsing Table
- LR语法分析算法 Algorithm4.44 LR-parsing algorithm

# 例 程序完成后打印下面的表格
|  |$      |c      |d      |C      |S     |
|--|-------|-------|-------|-------|------|
|1 |       |Shift  |Shift  |Goto   |Goto  | 
|3 |       |Shift  |Shift  |Goto   |      | 
|2 |Accept |       |       |       |      | 
|4 |       |Shift  |Shift  |Goto   |      | 
|5 |Reduce |Reduce |Reduce |       |      | 
|6 |Reduce |       |       |       |      | 
|7 |Reduce |Reduce |Reduce |       |      | 
# moves of an LR parser on input
| stack    | symbols   | input           | action                 |
| -------- | --------- | --------------- | ---------------------- |
|1         |           | id *  id +  id $ | 移入 id push state 6   |
|1 6       |  id       | *  id +  id $    | 规约 6 * push state 4  |
|1 4       |  F        | *  id +  id $    | 规约 4 * push state 3  |
|1 3       |  T        | *  id +  id $    | 移入 * push state 7    |
|1 3 7     |  T *      | id +  id $       | 移入 id push state 6   |
|1 3 7 6   |  T * id   | +  id $          | 规约 6 + push state 10 |
|1 3 7 10  |  T * F    | +  id $          | 规约 10 + push state 3 |
|1 3       |  T        | +  id $          | 规约 3 + push state 2  |
|1 2       |  S        | +  id $          | 移入 + push state 8    |
|1 2 8     |  S +      | id $             | 移入 id push state 6   |
|1 2 8 6   |  S + id   | $                | 规约 6 $ push state 4  |
|1 2 8 4   |  S + F    | $                | 规约 4 $ push state 11 |
|1 2 8 11  |  S + T    | $                | 规约 11 $ push state 2 |
|1 2       |  S        | $                | 接受，语法分析完成        |
