#include "mycan.h"
#include "oled.h"

volatile uint8_t can_string_ready = 0;
uint8_t rx_string[9] = {0};

/**
  * @brief  CAN 初始化并启动
  *         包含：过滤器配置、启动CAN、开启FIFO0接收通知
  */
void CAN_Start(void)
{
    CAN_FilterTypeDef can_filter_config = {0};

    /* ── 配置 CAN 过滤器 ── */
    can_filter_config.FilterActivation     = ENABLE;                    //开启过滤器
    can_filter_config.FilterBank           = 0;                         //调用第几个BANK
    can_filter_config.FilterMode           = CAN_FILTERMODE_IDMASK;     //IDMASK 模式
    can_filter_config.FilterScale          = CAN_FILTERSCALE_32BIT;     //使用32位过滤器
    can_filter_config.FilterIdHigh         = 0x0000;                    //不过滤
    can_filter_config.FilterIdLow          = 0x0000;                    //同上
    can_filter_config.FilterMaskIdHigh     = 0x0000;                    //同上
    can_filter_config.FilterMaskIdLow      = 0x0000;                    //同上
    can_filter_config.FilterFIFOAssignment = CAN_RX_FIFO0;              //分配到FIFO0
    if (HAL_CAN_ConfigFilter(&hcan, &can_filter_config) != HAL_OK)
    {
        return;
    }

    /* ── 启动 CAN ── */
    if (HAL_CAN_Start(&hcan) != HAL_OK)
    {
        return;
    }

    /* ── 开启 FIFO0 接收消息挂起中断通知 ── */
    HAL_CAN_ActivateNotification(&hcan, CAN_IT_RX_FIFO0_MSG_PENDING);
}


/**
  * @brief  通过 CAN 发送字符串（自动按 8 字节分包）
  * @param  hcan : CAN 句柄指针
  * @param  id   : 标准帧 ID
  * @param  str  : 待发送字符串（以 '\0' 结尾）
  */
void CAN_SendString(CAN_HandleTypeDef *hcan, uint32_t id, const char *str)
{
    CAN_TxHeaderTypeDef tx_header = {0};
    uint8_t data[8] = {0};
    uint32_t len = strlen(str);
    uint32_t offset = 0;
    uint32_t mailbox;

    tx_header.StdId = id;
    tx_header.ExtId = 0;
    tx_header.IDE   = CAN_ID_STD;
    tx_header.RTR   = CAN_RTR_DATA;

    while (offset < len)
    {
        uint32_t chunk_size = (len - offset > 8) ? 8 : (len - offset);

        memset(data, 0, sizeof(data));
        memcpy(data, str + offset, chunk_size);

        tx_header.DLC = chunk_size;

        while (HAL_CAN_GetTxMailboxesFreeLevel(hcan) == 0)
        {
        }

        if (HAL_CAN_AddTxMessage(hcan, &tx_header, data, &mailbox) != HAL_OK)
        {
            return;
        }

        offset += chunk_size;
    }
}

/**
  * @brief  在主循环中调用，用于显示最新接收到的 CAN 字符串
  */
void CAN_ProcessRxDisplay(uint8_t Line)
{
    char display_string[21] = {0};

    if (can_string_ready == 0)
    {
        return;
    }

    can_string_ready = 0;

    CombineData((uint8_t *)display_string,sizeof(display_string),rx_string);
    OLED_ShowString(Line, display_string);
}

/**
  * @brief  CAN 接收回调（FIFO0 有消息挂起时自动调用）
  * @note   弱定义，HAL 库在 stm32f1xx_hal_can.c 中已声明该回调函数原型
  */
void HAL_CAN_RxFifo0MsgPendingCallback(CAN_HandleTypeDef *hcan)
{
    CAN_RxHeaderTypeDef rx_header = {0};
    uint8_t rx_data[8] = {0};

    if (HAL_CAN_GetRxMessage(hcan, CAN_RX_FIFO0, &rx_header,rx_data) == HAL_OK)
    {
        uint8_t length = rx_header.DLC;

        if (length > 8)
        {
            length = 8;
        }

        memcpy(rx_string, rx_data, length);

        /* 补字符串结束符 */
        rx_string[length] = '\0';

        can_string_ready = 1;
    }
}

void CombineData(uint8_t *output,size_t output_size,const uint8_t *data)
{
    if (output == NULL || data == NULL || output_size == 0)
    {
        return;
    }

    snprintf((char *)output,output_size, "rec:%s",(const char *)data);
}