package com.mycompany.app;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollDomainSocketChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerDomainSocketChannel;
import io.netty.channel.unix.DomainSocketAddress;
import io.netty.channel.unix.ServerDomainSocketChannel;
import io.netty.buffer.PooledByteBufAllocator;


public class HelloWorld {
  public static void main(String[] args) throws Exception {
    org.apache.log4j.BasicConfigurator.configure();
    final ServerBootstrap bootstrap = new ServerBootstrap();
    bootstrap
      .group(new EpollEventLoopGroup(1), new EpollEventLoopGroup())
      .option(ChannelOption.SO_BACKLOG, 1024)
      .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
      .channel(EpollServerDomainSocketChannel.class)
      .childHandler(
         new ChannelInitializer<Channel>() {
           @Override
           protected void initChannel(
             final Channel channel)
             throws Exception {
             channel.pipeline().addLast(
                 new ChannelInboundHandlerAdapter() {

                    @Override
                    public void channelReadComplete(ChannelHandlerContext ctx) {
                      ctx.flush();
                    }

                    @Override
                    public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {

                      final ByteBuf buff = (ByteBuf) msg;
                      try {
                        byte[] bytes = new byte[buff.readableBytes()];
                        buff.getBytes(0, bytes);
                        System.out.println(new String(bytes));
                      } finally {
                        buff.release();
                      }

                       final ByteBuf bw = ctx.alloc().buffer();
                       bw.writeBytes("Hello Rayson".getBytes());
                       ctx.writeAndFlush(bw)
                           .addListener(
                               ChannelFutureListener.CLOSE
                          );

                      ctx.close();
                    }

                    @Override
                    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) throws Exception {
                      System.out.println("Error occur when reading from Unix domain socket: " + cause.getMessage());
                      ctx.close();
                    }
/*
                     @Override
                     public void channelActive(
                         final ChannelHandlerContext ctx)
                         throws Exception {
                         final ByteBuf buff = ctx.alloc().buffer();
                         buff.writeBytes("This is a test".getBytes());
                         ctx.writeAndFlush(buff)
                             .addListener(
                                 ChannelFutureListener.CLOSE
                             );
                     }
*/
                 }
             );
           }
         }
      );
    final ChannelFuture future =
    bootstrap.bind(new DomainSocketAddress("/tmp/amy")).sync();
    future.channel().closeFuture().sync();
  }
}
