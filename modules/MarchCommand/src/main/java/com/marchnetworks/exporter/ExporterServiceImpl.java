package com.marchnetworks.exporter;

import com.marchnetworks.command.export.ExporterCoreService;
import com.marchnetworks.command.export.ExporterException;
import com.marchnetworks.command.export.ExporterExceptionTypeEnum;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

public class ExporterServiceImpl implements ExporterCoreService
{
	public byte[] exportData( List<String> headerNames, List<List<String>> data ) throws ExporterException
	{
		if ( ( headerNames == null ) || ( data == null ) )
		{
			throw new ExporterException( ExporterExceptionTypeEnum.NO_DATA );
		}
		HSSFWorkbook workbook = new HSSFWorkbook();
		HSSFSheet sheet = workbook.createSheet();

		HSSFRow headerRow = sheet.createRow( 0 );
		short i = 0;
		for ( String headername : headerNames )
		{
			HSSFCell cell = headerRow.createCell( i++ );
			cell.setCellType( 1 );
			cell.setCellValue( headername );

			HSSFFont font = workbook.createFont();
			font.setBoldweight( ( short ) 700 );

			HSSFCellStyle style = workbook.createCellStyle();
			style.setFont( font );

			cell.setCellStyle( style );
		}

		int rowNum = 1;

		for ( List<String> array : data )
		{
			HSSFRow row = sheet.createRow( rowNum );
			rowNum++;
			short columnNum = 0;

			for ( String element : array )
			{
				HSSFCell cell = row.createCell( columnNum++ );
				if ( element != null )
					cell.setCellValue( element );
			}
		}

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try
		{
			workbook.write( bos );
			bos.flush();
			bos.close();
		}
		catch ( IOException e )
		{
			throw new ExporterException( ExporterExceptionTypeEnum.DATA_WRITE_ERROR );
		}

		byte[] bytes = bos.toByteArray();

		bos = null;

		return bytes;
	}
}
