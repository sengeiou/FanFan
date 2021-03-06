package com.fanfan.youtu.utils;

/**
 * Created by android on 2018/1/4.
 */

public class ErrorMsg {

    public static String getCodeDescribe(int code) {
        switch (code) {
            //sdk error
            case -1100:
                return "相似度错误";
            case -1101:
                return "人脸检测失败";
            case -1102:
                return "图片解码失败";
            case -1103:
                return "特征处理失败";
            case -1104:
                return "提取轮廓错误";
            case -1105:
                return "提取性别错误";
            case -1106:
                return "提取表情错误";
            case -1107:
                return "提取年龄错误";
            case -1108:
                return "提取姿态错误";
            case -1109:
                return "提取眼镜错误";
            case -1110:
                return "提取魅力值错误";
            case -1001:
                return "语音合成失败";
            //storage error
            case -1200:
                return "存储错误";
            case -1201:
                return "缓存错误";
            //logic error
            case -1300:
                return "图片为空";
            case -1301:
                return "参数为空";
            case -1302:
                return "个体已存在";
            case -1303:
                return "个体不存在";
            case -1304:
                return "参数过长";
            case -1305:
                return "人脸不存在";
            case -1306:
                return "组不存在";
            case -1307:
                return "组列表不存在";
            case -1308:
                return "url图片下载失败";
            case -1309:
                return "人脸个数超过限制";
            case -1310:
                return "个体个数超过限制";
            case -1311:
                return "组个数超过限制";
            case -1312:
                return "对个体添加了几乎相同的人脸";
            case -1313:
                return "参数不合法（特殊字符比如空格、斜线、tab、换行符）";
            case -1400:
                return "无效的图片格式";
            case -1401:
                return "图片模糊度检测失败";
            case -1402:
                return "美食图片检测失败";
            case -1403:
                return "图片下载失败";
            case -1404:
                return "算法模型调用错误";
            case -1405:
                return "提取图像指纹失败";
            case -1406:
                return "图像特征比对失败";
            case -1408:
                return "图片超出下载限制";
            case -1409:
                return "图片不满足检测要求";
            case -1505:
                return "图像请求URL的格式错误";
            case -1506:
                return "图像下载超时";
            case -1507:
                return "无法连接图像下载服务器";
            case -2000:
                return "文本为空";
            case -2001:
                return "文本过长";
            case -5101:
                return "OCR照片为空";
            case -5103:
                return "OCR识别失败";
            case -5106:
                return "身份证边框不完整";
            case -5107:
                return "输入图片不是身份证";
            case -5108:
                return "身份证信息不合规范";
            case -5109:
                return "照片模糊";
            case -5201:
                return "名片无足够文本";
            case -5202:
                return "名片文本行倾斜角度太大";
            case -5203:
                return "名片模糊";
            case -5204:
                return "名片姓名识别失败";
            case -5205:
                return "名片电话识别失败";
            case -5206:
                return "图像为非名片图像";
            case -5207:
                return "检测或者识别失败";
            case -5208:
                return "服务内部错误";
            case -7001:
                return "未检测到身份证，请对准边框(请避免拍摄时倾角和旋转角过大、摄像头)";
            case -7002:
                return "请使用第二代身份证件进行扫描";
            case -7003:
                return "不是身份证正面照片(请使用带证件照的一面进行扫描)";
            case -7004:
                return "不是身份证反面照片(请使用身份证反面进行扫描)";
            case -7005:
                return "确保扫描证件图像清晰";
            case -7006:
                return "请避开灯光直射在证件表面";
            case -9001:
                return "请求type错误，不是0，1";
            case -9002:
                return "OCR识别失败";
            case -9003:
                return "OCR识别失败";
            case -9010:
                return "银行卡OCR预处理错误";
            case -9011:
                return "银行卡OCR识别失败";
            case -9012:
                return "银行卡图片模糊";
            case -9013:
                return "不是银行卡";
            case -9014:
                return "卡号为空或不符合规范";
            case -9501:
                return "营业执照OCR";
            case -9502:
                return "营业执照OCR";
            case -9701:
                return "车牌OCR预处理错误";
            case -9702:
                return "车牌OCR识别失败";
            default:
                return "未知异常(" + code + ")";
        }
    }

}
