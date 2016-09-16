package com.xbw.mvp.core;

import com.xbw.mvp.tunnel.Config;
import com.xbw.mvp.tunnel.RawTunnel;
import com.xbw.mvp.tunnel.Tunnel;
import com.xbw.mvp.tunnel.httpconnect.HttpConnectConfig;
import com.xbw.mvp.tunnel.httpconnect.HttpConnectTunnel;
import com.xbw.mvp.tunnel.shadowsocks.ShadowsocksConfig;
import com.xbw.mvp.tunnel.shadowsocks.ShadowsocksTunnel;

import java.net.InetSocketAddress;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class TunnelFactory {
	
	public static Tunnel wrap(SocketChannel channel, Selector selector){
		return new RawTunnel(channel, selector);
	}
 
	public static Tunnel createTunnelByConfig(InetSocketAddress destAddress, Selector selector) throws Exception {
		if(destAddress.isUnresolved()){
			Config config=ProxyConfig.Instance.getDefaultTunnelConfig(destAddress);
			if(config instanceof HttpConnectConfig){
				return new HttpConnectTunnel((HttpConnectConfig)config,selector);
			}else if(config instanceof ShadowsocksConfig){
				return new ShadowsocksTunnel((ShadowsocksConfig)config,selector); 
			} 
			throw new Exception("The config is unknow.");
		}else {
			return new RawTunnel(destAddress, selector);
		}
	}

}
