#ifndef __OLED_H
#define __OLED_H

#include "main.h"
#include "i2c.h"
#include "gpio.h"
#include "cat.h"
#include "stdlib.h"
#include "math.h"
#include "font.h"
#include "string.h"

#define OLED_I2C_ADDR    0x78        // 默认地址，可改为 0x7A ，从7位换算为8位
#define OLED_WIDTH       128
#define OLED_HEIGHT      64

// 使用 HAL I2C 句柄
extern I2C_HandleTypeDef hi2c1;

void OLED_SendCmd(uint8_t cmd);
void OLED_Init(void);
void OLED_Clear(void);
void OLED_SendData(uint8_t data);
void OLED_NewFrame(uint8_t state);
void OLED_ShowFrame(void);
void OLED_SetGRAM(const uint8_t *frame_data);
void OLED_ShowCatAnimation(uint16_t delay_ms, uint16_t loop_count);
void OLED_ShowPicture(const uint8_t *pic_data);
void OLED_SetPoint(uint8_t x,uint8_t y);
void OLED_DrawLine(int x1,int y1,int x2,int y2);
void OLED_DrawWord(uint8_t Line,char ch);
void OLED_ShowString(uint8_t Line, const char *str);


//行的定义
#define LINE_0 0xB7
#define LINE_1 0xB6
#define LINE_2 0xB5
#define LINE_3 0xB4
#define LINE_4 0xB3
#define LINE_5 0xB2
#define LINE_6 0xB1
#define LINE_7 0xB0

#endif
