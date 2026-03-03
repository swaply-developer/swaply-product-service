package com.ch.swaplyproduct.util;

import jdk.dynalink.StandardOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

// 호스트안에 저장된 엑셀을 분석하여, 이미지가 발견되면 호스트의 지정된 디렉토리에 복사
// 이미지가 아닌 일반 텍스트 정보는 추출하여 Mysql 에 넣을 것임..

@Slf4j
@Component
public class ExcelParser {

    //엑셀의 위치를 넘겨받아 해당엑셀을 분석할 하여야 함, 따라서 매게변수로 엑셀의 풀 경로를 받아야 함
    public void parseAndSaveImages(String excelPath) throws Exception{
        /*------------------------------------------------
        *   1. 파일이 진짜 존재하는지 체크
        *------------------------------------------------ */
        Path excelFile=Path.of(excelPath);
        // 파일이 존재하지 않거나, 혹은 존재하더라도 파일 형태가 아닌 디렉토리 라면.. (분석불가) 진행하지 않음
        if(!Files.exists(excelFile) || Files.isDirectory(excelFile)){
            throw new IllegalArgumentException("엑셀 파일이 없거나, 경로가 잘못됨"+excelFile.toAbsolutePath());
        }

        /*------------------------------------------------
         *   1. 엑셀파일이 들어있는 부모 디렉토리 경로 뽑아내기
         *------------------------------------------------ */
        Path uploadDir = excelFile.getParent();

        // 엑셀 파일을 열어서 분석 시작
        // try~ with~ resource(데이터베이스, 스트림 (IO) 등에서 필수적으로 처리하는
        // finally 문을 줄여 쓰는 방법
        try(FileInputStream fis = new FileInputStream(excelFile.toFile());
            // 97~2003 구 엑셀 .xls HSSFWorkbook, 2003 이후  신엑셀 .xlsx XSSFWorkbook로 접근
            XSSFWorkbook wb = (XSSFWorkbook) WorkbookFactory.create(fis)
        ){
            /*------------------------------------------------
             *   3. 엑셀 파일 열기 성공
             *        시트 - 행 - 열 순으로 접근
             *------------------------------------------------ */
            XSSFSheet sheet = wb.getSheetAt(0);

            // POI 는 이미지를 도형의 일부로 생각함 즉 Shape 모든 그려지는 요소의 최상위 개념임..
            XSSFDrawing drawing =sheet.getDrawingPatriarch();

            // 만약 시트에 그림이 하나도 없으면 메시지 남기지..
            if(drawing == null){
                log.debug("엑셀 시트에 이미지가 없어");
                return;
            }
            // 시트에 있는 모든 Shape을 하나씩 꺼내서 검사하자!!
            for(XSSFShape shape: drawing.getShapes()){
                if(shape instanceof XSSFPicture pic){
                    // 그림의 위치를 찾고, 그 그림이 속한 행들의 텍스트도 추출
                    XSSFClientAnchor anchor = (XSSFClientAnchor) pic.getAnchor();

                    // 발견된 이미지가 몇번째 행에 존재하는지를 알아보자
                    int rowIndex = anchor.getRow1();
                    XSSFRow row =sheet.getRow(rowIndex); // 이미지의 행을 쉬트에게 물어봐서 얻어옴
                    String categoryId = getCellValue(row.getCell(0));   // subcategory_id
                    String productName = getCellValue(row.getCell(2));  // 상품명
                    String brand = getCellValue(row.getCell(3));
                    String price = getCellValue(row.getCell(4));

                    log.debug("subcategory={}, product_name={}, brand={}, price={}",categoryId,productName,brand,price);

                    // 이미지를 꺼내서, subcategory_id의 숫자 값을 조합한 디렉토리를 생성하여 이미지 명 또한 img_현재시간 형태로 저장
                    // ex) product2/img_현재시간.jpg
                    String dirName = "product" + categoryId;    // product2, product3

                    // 엑셀이 있던 그 디렉토리 경로 얻기 ( 여기에 다가 product2, product3, .. 만들려고)
                    Path productDir = uploadDir.resolve(dirName);   //~~ tmp/saga-product/upload/product2
                    Files.createDirectories(productDir);    // 디렉토리 생성

                    //img + 현재시간 + .확장자 (기존확장자 얻기)
                    XSSFPictureData  data =pic.getPictureData();   // 이미지 정보를 얻기
                    String ext = data.suggestFileExtension();    // 이미지 확장자 얻기

                    String imgName = "img"+System.currentTimeMillis()+"."+ext;
                    Path out = productDir.resolve(imgName);
                    //파일생성
//                    Files.writeString( Path객체, 이미지byte, 생성옵션,옵션)  실제 하드디스크
                    Files.write(out,data.getData(), StandardOpenOption.CREATE,StandardOpenOption.TRUNCATE_EXISTING);
                    log.debug("엑셀에서 추출하여 저장한 이미지는 {} ", imgName);
                }

            }
        }

    }
    /*------------------------------------------------
     *   엑셀의 각 칸에 들어있는 데이터 꺼내기 (메서드 따로함)
     *      POI 는 각 셀에 들어있는 데이터의 종류별로 타입을 구분하는데,
     *       데이터를 접근할때는 반드시 타입에 맞는 메서드를 호출해야함,
     *  따라서 조건문이 필수적으로 사용됨,
     *      조건문을 깔끔하게 처리하기 위해 아래의 메서드를 처리
     *------------------------------------------------ */
    private String getCellValue(XSSFCell cell){

        if(cell == null)return "";

        return switch (cell.getCellType()){
            case STRING ->  cell.getStringCellValue().trim();   // 셀에 들어잇는 데이터가 문자열일 경우 getStringCellValue()
            case NUMERIC -> String.valueOf((long)cell.getNumericCellValue());  //
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> "";
        };
    }


}
