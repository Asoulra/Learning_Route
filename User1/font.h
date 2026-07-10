#ifndef __FONT_H_
#define __FONT_H_

#include <stdint.h>
//字符类型，之后可以自己声明汉字等
extern const uint8_t Font6x8_Num[10][6];
extern const uint8_t Font6x8_Upper[26][6];
extern const uint8_t Font6x8_Lower[26][6];
extern const uint8_t Font6x8_Symbol[7][6];

//函数定义
const uint8_t * FindFont(char ch);

#endif // !__FONT_H_
