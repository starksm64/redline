package org.freecompany.redline;

import org.freecompany.redline.header.*;
import org.freecompany.redline.payload.*;
import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.zip.*;

public class Copier {

	public static void main( String[] args) throws Exception {
		new Copier().run( Channels.newChannel( System.in), new FileOutputStream( args[ 0]).getChannel());
	}

	public void run( ReadableByteChannel in, WritableByteChannel out) throws Exception {
		Format format = new Scanner().run( in);
		format.write( out);
		in = Channels.newChannel( new GZIPInputStream( Channels.newInputStream( in))); 
		out = Channels.newChannel( new GZIPOutputStream( Channels.newOutputStream( out)));
		
		ByteBuffer buffer = ByteBuffer.allocate( 4096);
		CpioHeader header;
		do {
			header = new CpioHeader();
			header.read( in);
			header.write( out);
			int remaining = Util.round( header.getFileSize(), 3);
			while ( remaining > 0) {
				if ( remaining < buffer.capacity()) buffer.limit( remaining);
				remaining -= in.read( buffer);
				buffer.flip();
				while ( buffer.hasRemaining()) out.write( buffer);
				buffer.clear();
				if ( buffer.limit() != buffer.capacity()) buffer.reset();
			}
		} while ( !header.isLast());
		out.close();
		in.close();
	}
}