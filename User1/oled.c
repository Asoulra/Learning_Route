#include "oled.h"
#include "string.h"

/**
 * @brief 反转字节的位顺序
 * @param  b  待反转的字节
 * @return    位顺序反转后的字节
 * @note       用于将I2C发送的字节位序转换为OLED屏幕需要的格式
 */
static uint8_t OLED_ReverseBits(uint8_t b)
{
    b = (b & 0xF0) >> 4 | (b & 0x0F) << 4;
    b = (b & 0xCC) >> 2 | (b & 0x33) << 2;
    b = (b & 0xAA) >> 1 | (b & 0x55) << 1;
    return b;
}

/**
 * @brief       发送命令到OLED屏幕
 * @param  cmd  要发送的命令字节
 * @note         OLED屏幕采用I2C接口，命令格式为: 控制字节0x00 + 命令字节
 */
void OLED_SendCmd(uint8_t cmd)
{
    uint8_t send_buf[2];
    send_buf[0] = 0x00;
    send_buf[1] = cmd;
    HAL_I2C_Master_Transmit(&hi2c1, OLED_I2C_ADDR, send_buf, 2, HAL_MAX_DELAY);
}

/**
 * @brief       发送数据到OLED屏幕
 * @param  data 要发送的数据字节
 * @note         OLED屏幕采用I2C接口，数据格式为: 控制字节0x40 + 数据字节
 */
void OLED_SendData(uint8_t data)
{
    uint8_t send_buf[2];
    send_buf[0] = 0x40;
    send_buf[1] = data;
    HAL_I2C_Master_Transmit(&hi2c1, OLED_I2C_ADDR, send_buf, 2, HAL_MAX_DELAY);
}

/**
 * @brief      初始化OLED屏幕
 * @details    配置OLED显示参数，包括:
 *             - 时钟分频因子
 *             - 驱动路数
 *             - 显示偏移
 *             - 起始行
 *             - 电荷泵
 *             - 内存地址模式
 *             - 段重定义
 *             - COM扫描方向
 *             - 对比度
 *             - 预充电周期
 *             - VCOMH电压
 *             - 显示模式
 */
void OLED_Init(void)
{
    OLED_SendCmd(0xAE);
    OLED_SendCmd(0xD5);
    OLED_SendCmd(80);
    OLED_SendCmd(0xA8);
    OLED_SendCmd(0X3F);
    OLED_SendCmd(0xD3);
    OLED_SendCmd(0X00);
    OLED_SendCmd(0x40);
    OLED_SendCmd(0x8D);
    OLED_SendCmd(0x14);
    OLED_SendCmd(0x20);
    OLED_SendCmd(0x02);
    OLED_SendCmd(0xA1);
    OLED_SendCmd(0xC0);
    OLED_SendCmd(0xDA);
    OLED_SendCmd(0x12);
    OLED_SendCmd(0x81);
    OLED_SendCmd(0xEF);
    OLED_SendCmd(0xD9);
    OLED_SendCmd(0xf1);
    OLED_SendCmd(0xDB);
    OLED_SendCmd(0x30);
    OLED_SendCmd(0xA4);
    OLED_SendCmd(0xA6);
    OLED_SendCmd(0xAF);
}

/**
 * @brief      清空OLED屏幕显示
 * @details    创建一帧全黑画面并显示到屏幕上
 */
void OLED_Clear(void)
{
    OLED_NewFrame(0);
    OLED_ShowFrame();
}

const uint8_t *catData[15] = {
    data1,
    data2,
    data3,
    data4,
    data5,
    data6,
    data7,
    data8,
    data9,
    data10,
    data11,
    data12,
    data13,
    data14,
    data15
};

/**
 * @brief OLED屏幕的GRAM显示缓冲区
 * @details 8页 x 128列的位图数据，每位代表一个像素点
 */
uint8_t GRAM[8][128];

/**
 * @brief       设置GRAM显示缓冲区数据
 * @param  frame_data  指向帧数据的指针(128x64位图数据)
 * @details      将帧数据复制到GRAM缓冲区，并进行位序反转处理
 * @note         帧数据格式: 8页，每页128字节
 */
void OLED_SetGRAM(const uint8_t *frame_data)
{
    for (int i = 0; i < 8; i++)
    {
        for (int j = 0; j < 128; j++)
        {
            GRAM[7-i][j] = OLED_ReverseBits(frame_data[i * 128 + j]);
        }
    }
}

/**
 * @brief       创建新帧(清空GRAM缓冲区)
 * @param  state 填充值，0为清空(黑屏)，0xFF为填充(白屏)
 * @details      将GRAM缓冲区的所有字节设置为指定值
 */
void OLED_NewFrame(uint8_t state)
{
    memset(GRAM, state, sizeof(GRAM));
}

/**
 * @brief      将GRAM缓冲区的当前帧数据显示到OLED屏幕
 * @details    遍历GRAM的8页数据，通过I2C发送到OLED屏幕显示
 */
void OLED_ShowFrame(void)
{
    uint8_t sendbuf[129];
    sendbuf[0] = 0x40;

    for (int i = 0; i < 8; i++)
    {
        for (int j = 0; j < 128; j++)
        {
            sendbuf[j + 1] = GRAM[i][j];
        }

        OLED_SendCmd(0xB0 + i);
        OLED_SendCmd(0x00);
        OLED_SendCmd(0x10);

        HAL_I2C_Master_Transmit(&hi2c1, OLED_I2C_ADDR, sendbuf, sizeof(sendbuf), HAL_MAX_DELAY);
    }
}

/**
 * @brief       显示月薪猫动画的一帧
 * @param  delay_ms    帧间延迟(毫秒)
 * @param  loop_count  循环次数，0表示无限循环
 * @details      根据direction方向递增或递减frame，在指定延迟后更新显示
 *               动画在帧1-14之间来回播放
 */
void OLED_ShowCatAnimation(uint16_t delay_ms, uint16_t loop_count)
{
    static uint32_t last_tick = 0;
    static int8_t frame = 0;
    static int8_t direction = 1;
    static uint16_t loop = 0;

    if (HAL_GetTick() - last_tick < delay_ms)
    {
        return;
    }

    last_tick = HAL_GetTick();

    OLED_SetGRAM(catData[frame]);
    OLED_ShowFrame();

    if (direction == 1)
    {
        frame++;

        if (frame >= 14)
        {
            frame = 14;
            direction = -1;
        }
    }
    else
    {
        frame--;

        if (frame <= 1)
        {
            frame = 1;
            direction = 1;

            if (loop_count != 0)
            {
                loop++;

                if (loop >= loop_count)
                {
                    loop = 0;
                    frame = 0;
                    direction = 1;
                    return;
                }
            }
        }
    }
}

/**
 * @brief       显示静态图片
 * @param  pic_data  指向图片数据的指针(128x64位图)
 */
void OLED_ShowPicture(const uint8_t *pic_data)
{
    OLED_SetGRAM((uint8_t *)pic_data);
    OLED_ShowFrame();
}

/**
 * @brief       在指定坐标设置一个像素点
 * @param  x    x坐标(0-127)
 * @param  y    y坐标(0-63)
 * @details      根据y坐标计算所属页和页内位号，将对应像素置1
 */
void OLED_SetPoint(uint8_t x, uint8_t y)
{
    uint8_t page = y / 8;
    uint8_t bit = y % 8;
    uint8_t col = x;
    GRAM[page][col] |= (1 << bit);
}

/**
 * @brief       在两点之间绘制一条直线
 * @param  x1   起点x坐标
 * @param  y1   起点y坐标
 * @param  x2   终点x坐标
 * @param  y2   终点y坐标
 * @details      使用Bresenham算法绘制直线，绘制完成后自动刷新显示
 */
void OLED_DrawLine(int x1, int y1, int x2, int y2)
{
    int dx = abs(x2 - x1);
    int dy = abs(y2 - y1);

    int sx = (x1 < x2) ? 1 : -1;
    int sy = (y1 < y2) ? 1 : -1;

    int err = dx - dy;

    while (1)
    {
        if (x1 >= 0 && x1 < 128 && y1 >= 0 && y1 < 64)
        {
            OLED_SetPoint(x1, y1);
        }

        if (x1 == x2 && y1 == y2)
        {
            break;
        }

        int e2 = 2 * err;

        if (e2 > -dy)
        {
            err -= dy;
            x1 += sx;
        }

        if (e2 < dx)
        {
            err += dx;
            y1 += sy;
        }
    }
    OLED_ShowFrame();
}

/**
 * @brief       在指定行显示单个字符
 * @param  Line 行地址命令(LINE_0 ~ LINE_7)
 * @param  ch   要显示的字符
 * @details      查找字符点阵并通过I2C发送到OLED屏幕显示
 */
void OLED_DrawWord(uint8_t Line, char ch)
{
    const uint8_t *temp_Font;
    temp_Font = FindFont(ch);
    OLED_SendCmd(Line);
    for (int i = 0; i < 6; i++)
    {
        OLED_SendCmd(0x00 + i);
        OLED_SendCmd(0x10);
        OLED_SendData(OLED_ReverseBits(temp_Font[i]));
    }
}

/**
 * @brief       在指定行显示字符串
 * @param  Line 行地址命令(LINE_0 ~ LINE_7)
 * @param  str  要显示的字符串
 * @details      查找每个字符的点阵数据并批量发送到OLED屏幕
 *               最大支持20个字符，超长字符串会提示"String too long"
 */
void OLED_ShowString(uint8_t Line, const char *str)
{
    uint16_t len = strlen(str);
    if (len > 20)
    {
        OLED_ShowString(Line, "String too long");
        return;
    }
    OLED_SendCmd(Line);
    OLED_SendCmd(0x00);
    OLED_SendCmd(0x10);
    uint8_t temp[20][6];
    for (int i = 0; i < len; i++)
    {
        memcpy(temp[i], FindFont(str[i]), 6);
    }

    uint8_t sendbuf[121];
    sendbuf[0] = 0x40;
    for (int i = 0; i < len; i++)
    {
        for (int j = 0; j < 6; j++)
        {
            sendbuf[1 + i * 6 + j] = OLED_ReverseBits(temp[i][j]);
        }
    }

    HAL_I2C_Master_Transmit(&hi2c1, OLED_I2C_ADDR, sendbuf, len * 6 + 1, HAL_MAX_DELAY);
}
