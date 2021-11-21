import psutil
import sys
import socket
import time
from pyzbar.pyzbar import decode
from PIL import Image, ImageGrab

# 组播组IP和端口
mcast_group_ip = '239.0.1.255'
mcast_group_port = 12585

def parse_pic():
    im = ImageGrab.grabclipboard()
    print('getting img...')
    if isinstance(im, Image.Image):
        print('found image.')
        result = decode(im)
        if (len(result) >= 1):
            url = result[0].data.decode('utf-8')
            print(url)
            send(url)
            time.sleep(1)
            clear_clipboard()
def main():
    while True:
        parse_pic()
        time.sleep(1)
def clear_clipboard():
    from ctypes import windll
    if windll.user32.OpenClipboard(None):  # 打开剪切板
        windll.user32.EmptyClipboard()  # 清空剪切板
        windll.user32.CloseClipboard()  # 关闭剪切板
def send(url):
    info = psutil.net_if_addrs()
    for k,v in info.items():
        for item in v:
            if item[0] == 2 and not item[1]=='127.0.0.1':
                print('send msg on'+k)
                send_sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM, socket.IPPROTO_UDP)
                try:
                    local_ip = socket.gethostbyname(item[1])
                    send_sock.bind((local_ip, mcast_group_port))
                    message = "{\"scanner_data\":{\"url\":\"%s\",\"t\":%d}}" % (url,int(time.time()))
                    print(message)
                    send_sock.sendto(message.encode(), (mcast_group_ip, mcast_group_port))
                    print(f'{time.strftime("%Y-%m-%d %H:%M:%S", time.localtime())}: message send finish')
                except OSError:
                    print('send msg on '+k+' failed.')

if __name__ == '__main__':
    main()

## package cmd --> pyinstaller --clean -F main.py --collect-all pyzbar