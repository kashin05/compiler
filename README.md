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
