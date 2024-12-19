from rembg import remove
from PIL import Image
import numpy as np
import os

def extract_seal(input_path, output_path):
    """提取并优化印章图像"""
    # 读取输入图片
    input_image = Image.open(input_path)
    
    # 使用 rembg 移除背景
    output_image = remove(
        input_image,
        alpha_matting=True,
        alpha_matting_foreground_threshold=240,
        alpha_matting_background_threshold=10,
        alpha_matting_erode_size=10
    )
    
    # 转换为 numpy 数组处理
    img_array = np.array(output_image)
    
    # 获取各个通道
    r, g, b, a = img_array[:, :, 0], img_array[:, :, 1], img_array[:, :, 2], img_array[:, :, 3]
    
    # 优化的印章检测条件
    white_pixels = (r > 220) & (g > 220) & (b > 220)
    gray_pixels = (abs(r - g) < 15) & (abs(g - b) < 15) & (abs(r - b) < 15)
    is_seal_color = (r > 150) & (r > g * 1.5) & (r > b * 1.5)
    color_diff = np.maximum(np.maximum(r, g), b) - np.minimum(np.minimum(r, g), b)
    similar_colors = color_diff < 20
    
    # 确定要移除的像素
    pixels_to_remove = (white_pixels | gray_pixels | similar_colors) & (~is_seal_color)
    img_array[:, :, 3][pixels_to_remove] = 0
    
    # 增强印章颜色
    valid_pixels = img_array[:, :, 3] > 0
    for i in range(3):
        channel = img_array[:, :, i]
        channel[valid_pixels] = np.clip(channel[valid_pixels] * 1.1, 0, 255)
    
    # 保存结果
    result_image = Image.fromarray(img_array)
    result_image.save(output_path)
    return img_array

def process_seal_complete(input_path, output_path):
    """处理印章的主函数"""
    extract_seal(input_path, output_path)