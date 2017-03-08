import static java.lang.Math.*;

import java.util.ArrayList;

/**
* @author Penguin8885
* このプログラムは自作のC言語プログラムから移植したためほぼオブジェクト指向に従っていません．
*/
public class FormulaCalculator {

     /*式中の空白削除*/
     private static String deleteSpaces(String formula){
          String deleted;
          deleted = formula.replace(" ", "");          //半角スペース削除
          deleted = deleted.replace("\t", "");     //タブ削除
          return deleted;
     }

     /*括弧の数が適切であるか確認する*/
     private static boolean isProperBracketNumber(String formula){
          int left, right;

          left = right = 0;     //カウント初期化

          /*括弧検索*/
          for(int i = 0; i < formula.length(); i++){
               char c = formula.charAt(i);
               if(c == '(')
                    left++;
               else if(c == ')')
                    right++;
          }
          if(left == right)
               return true;     //適切である
          else
               return false;     //適切でない
     }

     /*引数の文字列の一番外側に括弧を追加*/
     private static String putInBrackets(String formula){
          StringBuilder buf = new StringBuilder("(");
          buf.append(formula);
          buf.append(")");
          return buf.toString();
     }

     /*式中に括弧があるかを判別する*/
     private static boolean hasBrackets(String formula){
          for(int i = 0; i < formula.length(); i++){
               char c = formula.charAt(i);
               if(c == '(' || c == ')')
                    return true;
          }
          return false;
     }

     /*式の引数の位置の文字がオペレータかを判定する*/
     private static boolean isOperator(String formula, int index){
          char c = formula.charAt(index);
          if(c == '+' || c == '-' || c == '*' || c == '/' || c == '^')
               return true;
          else
               return false;
     }

     /*引数の文字列がオペランドとして使用可能か判定する*/
     private static boolean isOperand(String operand){
          try{
               Double.parseDouble(operand);
          }
          catch(NumberFormatException err){
               return false;
          }
          return true;
     }

     /*式中の引数とそれ以外で3分割する*/
     private static String[] splitAtParameter(String formula){
          OutsideBracketsIndex bs = new OutsideBracketsIndex(formula);
          int leftIndex = bs.getLeftIndex();
          int rightIndex = bs.getRightIndex();

          String[] splited = new String[3];
          splited[0] = formula.substring(0, leftIndex + 1);
          splited[1] = formula.substring(leftIndex + 1, rightIndex);
          splited[2] = formula.substring(rightIndex);

          return splited;
     }

     /*式中の関数とそれ以外で3分割する*/
     private static String[] splitAtFunction(String formula){
          OutsideBracketsIndex bs = new OutsideBracketsIndex(formula);
          int leftIndex = bs.getLeftIndex();
          int rightIndex = bs.getRightIndex();

          int operatorIndex = -1;     //関数名の前のオペレータのインデックス
          for(int i = leftIndex; i >= 0; i--){
               if(isOperator(formula, i) == true || formula.charAt(i) == ','){
                    operatorIndex = i;
                    break;
               }
          }

          String[] splited = new String[3];
          splited[0] = formula.substring(0, operatorIndex + 1);
          splited[1] = formula.substring(operatorIndex + 1, rightIndex + 1);
          splited[2] = formula.substring(rightIndex + 1);

          return splited;
     }

     /*3分割された式を連結する*/
     private static String concatSplitedFormula(String[] splited){
          if(splited.length != 3)
               return null;
          return splited[0].concat(splited[1]).concat(splited[2]);
     }

     /*式をオペランドとオペレータに分割する*/
     private static ArrayList<String> dividIntoParts(String formula){
          ArrayList<String> divided = new ArrayList<String>();
          int operatorIndex1 = -1, operatorIndex2 = -1;     //一つ前とその次のオペレーターのインデックス

          /*
          * オペレータを区切りとして分割する
          * 最初の文字がオペレータであっても分割数は変わらないため i = 1 から実行
          */
          for(int i = 1; i < formula.length(); i++){
               char c = formula.charAt(i);
               if(isOperator(formula, i) == true || c == ','){
                    if((c == '+' || c == '-') && operatorIndex2 == i - 1) continue;     //単項演算子があった場合は飛ばす
                    operatorIndex1 = operatorIndex2;
                    operatorIndex2 = i;
                    divided.add(formula.substring(operatorIndex1 + 1, operatorIndex2));          //オペランド切り出し
                    divided.add(formula.substring(operatorIndex2, operatorIndex2 + 1));          //オペレータ切り出し
               }
          }
          divided.add(formula.substring(operatorIndex2 + 1));          //最後のオペランドを切り出す

          return divided;
     }

     /*与えられたオペランドとオペレータに従って計算を行う*/
     private static String calculate(String operand1, String operator, String operand2){
          switch(operator.charAt(0)){
          case '+':
               return Double.toString(Double.parseDouble(operand1) + Double.parseDouble(operand2));
          case '-':
               return Double.toString(Double.parseDouble(operand1) - Double.parseDouble(operand2));
          case '*':
               return Double.toString(Double.parseDouble(operand1) * Double.parseDouble(operand2));
          case '/':
               return Double.toString(Double.parseDouble(operand1) / Double.parseDouble(operand2));
          case '^':
               return Double.toString(pow(Double.parseDouble(operand1) , Double.parseDouble(operand2)));
          default:
               return null;
          }
     }

     /*与えられたオペランドとオペレータの式に従って計算を行う*/
     private static ArrayList<String> calculate(ArrayList<String> parts){
          /*最優先演算子(^)について計算*/
          for(int i = 1; i < parts.size(); i += 2){
               if(parts.get(i).equals("^")){
                    String answer = calculate(parts.get(i - 1), "^", parts.get(i + 1));
                    /*計算式と計算結果の置き換え*/
                    parts.add(i + 2 , answer);
                    parts.remove(i + 1);
                    parts.remove(i);
                    parts.remove(i - 1);

                    i = -1;     //はじめから検索再開(再開時にi += 2されるためi = -1)
               }
          }
          /*優先演算子(*,/)について計算*/
          for(int i = 1; i < parts.size(); i += 2){
               String operater = parts.get(i);
               if(operater.equals("*") || operater.equals("/")){
                    String answer = calculate(parts.get(i - 1), operater, parts.get(i + 1));
                    /*計算式と計算結果の置き換え*/
                    parts.add(i + 2 , answer);
                    parts.remove(i + 1);
                    parts.remove(i);
                    parts.remove(i - 1);

                    i = -1;     //はじめから検索再開(再開時にi += 2されるためi = -1)
               }
          }
          /*通常演算子(+,-)について計算*/
          for(int i = 1; i < parts.size(); i += 2){
               String operater = parts.get(i);
               if(operater.equals("+") || operater.equals("-")){
                    String answer = calculate(parts.get(i - 1), operater, parts.get(i + 1));
                    /*計算式と計算結果の置き換え*/
                    parts.add(i + 2 , answer);
                    parts.remove(i + 1);
                    parts.remove(i);
                    parts.remove(i - 1);

                    i = -1;     //はじめから検索再開(再開時にi += 2されるためi = -1)
               }
          }
          return parts;
     }

     /*与えられた関数とオペランド，オペレーターに従って計算を行う*/
     private static String calculateFunction(String func, ArrayList<String> parts){
          /*
          * <実装未定>
          * asinh acosh atanh
          *
          * cosec sec cotan
          * acosec asec acotan
          *
          * rand
          * round
          * mod fmod
          */
          if(parts == null) return null;

          int hash = func.hashCode();     //関数の文字列ハッシュコード
          double answer = 0;               //答えの値

          if(parts.size() == 1){     //関数の引数が1つのとき
               double operand = Double.parseDouble(parts.get(0));

               switch(hash){
               case 0:               //()だけの無名関数
                    answer = operand; break;
               case 96370:          //abs
                    answer = abs(operand); break;
               case 110534571:     //todeg
                    answer = toDegrees(operand); break;
               case 110547898:     //torad
                    answer = toRadians(operand); break;
               case 3538208:     //sqrt
                    answer = sqrt(operand); break;
               case 3047137:     //cbrt
                    answer = cbrt(operand); break;
               case 113880:     //sin
                    answer = sin(operand); break;
               case 98695:          //cos
                    answer = cos(operand); break;
               case 114593:     //tan
                    answer = tan(operand); break;
               case 3003607:     //asin
                    answer = asin(operand); break;
               case 2988422:     //acos
                    answer = acos(operand); break;
               case 3004320:     //atan
                    answer = atan(operand); break;
               case 3530384:     //sinh
                    answer = sinh(operand); break;
               case 3059649:     //cosh
                    answer = cosh(operand); break;
               case 3552487:     //tanh
                    answer = tanh(operand); break;
               case 100893:     //exp
                    answer = exp(operand); break;
               case 107332:     //log
                    answer = log(operand); break;
               case 103147619:     //log10
                    answer = log(operand) / log(10.0); break;
               default: return null;
               }

          }else if(parts.size() == 3){     //関数の引数が2つのとき
               double operand1 = Double.parseDouble(parts.get(0));
               double operand2 = Double.parseDouble(parts.get(2));

               switch(hash){
               case 111192:     //pow
                    answer = pow(operand1, operand2); break;
               default: return null;
               }

          }else
               return null;

          if(Double.isNaN(answer) == true){
               System.out.println("\t NaN");
               return null;     //答えの値がNaNのとき回避
          }
          else return Double.toString(answer);
     }

     /*分割された式から関数名を返却する*/
     private static String getFunctionName(String splited){
          int length = splited.length();
          if(length == 1)
               return "";
          else{
               return splited.substring(0, length - 1);
          }
     }

     /*与えられた関数を計算する*/
     private static String calculateFunction(String formula){
          String splitedAtBrackets[] = splitAtParameter(formula);     //関数の引数を取り出す

          /*関数の引数の中に関数がある場合はそれを計算する*/
          while(hasBrackets(splitedAtBrackets[1]) == true){
               String splitedAtFunction[] = splitAtFunction(splitedAtBrackets[1]);     //式中の関数を取り出す
               splitedAtFunction[1] = calculateFunction(splitedAtFunction[1]);          //関数の値を計算し更新
               if(splitedAtFunction[1] == null) return null;                              //計算に失敗した場合nullを返却
               splitedAtBrackets[1] = concatSplitedFormula(splitedAtFunction);          //式を再結合
          }

          ArrayList<String> parts = dividIntoParts(splitedAtBrackets[1]);     //関数の引数をオペランドとオペレーターに分割する
          /*すべてのオペランドが数値として使用可能か確認*/
          for(int i = 0; i < parts.size(); i += 2){
               if(isOperand(parts.get(i)) == false)
                    return null;
          }
          /*関数名と引数の計算をし，その関数に従って計算した値を返す*/
          return calculateFunction(getFunctionName(splitedAtBrackets[0]), calculate(parts));
     }

     /*与えられた式を計算する*/
     public static String calculateFormula(String formula){
          formula = deleteSpaces(formula);     //不要なスペース削除
          if(isProperBracketNumber(formula) == false){     //括弧の数を確認
               System.out.println("error: cannot calculate");
               return null;
          }
          formula = putInBrackets(formula);     //一番外側に括弧をつけて関数化

          String answer = calculateFunction(formula);     //計算
          if(answer == null)
               System.out.println("error: cannot calculate");     //計算できないとき警告
          else
               System.out.println(answer.toString());

          return answer;
     }

     public static void main(String[] args){
         String ans = FormulaCalculator.calculateFormula("abs(1-4)*2+4/2");
         System.out.println(ans);
     }

}

class OutsideBracketsIndex{
     private int leftIndex = -1;
     private int rightIndex = -1;

     /*コンストラクタ*/
     public OutsideBracketsIndex(){}
     public OutsideBracketsIndex(String formula){ setIndex(formula); }

     /*式を与える*/
     public void setFormula(String formula){
          setIndex(formula);
     }
     /*括弧のインデックスを検索*/
     private void setIndex(String formula){
          int i;
          /*左側の括弧を検索*/
          for(i = 0; i < formula.length(); i++){
               if(formula.charAt(i) == '('){
                    leftIndex = i;
                    break;
               }
          }

          /*左側括弧に対応する右側の括弧を検索*/
          int leftCount = 0;     //検索した左括弧以外の関係しない左括弧の数
          for(i++; i < formula.length(); i++){
               char c = formula.charAt(i);
               if(c == '('){
                    leftCount++;          //関係しない左括弧を見つけた場合はカウントをインクリメント
               }else if(c == ')'){
                    if(leftCount != 0){
                         leftCount--;     //関係しない左括弧に対応する右括弧を見つけた場合はカウントをデクリメント
                    }else{
                         rightIndex = i;
                         break;
                    }
               }
          }
     }
     /*左括弧のインデックスを返却*/
     public int getLeftIndex(){
          return leftIndex;
     }
     /*右括弧のインデックスを返却*/
     public int getRightIndex(){
          return rightIndex;
     }

}
