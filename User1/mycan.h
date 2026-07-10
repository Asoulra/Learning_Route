#ifndef __MYCAN_H
#define __MYCAN_H

#ifdef __cplusplus
extern "C" {
#endif

#include "main.h"
#include "can.h"
#include "string.h"
#include <stdint.h>
#include <stddef.h>
#include <stdio.h>

extern uint8_t rx_string[9];

void CAN_ProcessRxDisplay(uint8_t Line);
void CAN_Start(void);
void CAN_SendString(CAN_HandleTypeDef *hcan, uint32_t id, const char *str);
void CombineData(uint8_t *output,size_t output_size,const uint8_t *data);

#ifdef __cplusplus
}
#endif

#endif
