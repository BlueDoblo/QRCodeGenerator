
package JDE;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import javax.imageio.ImageIO;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.MediaPrintableArea;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.Sides;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;
import java.util.Properties;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.printing.PDFPageable;
import org.apache.pdfbox.printing.PDFPrintable;
import org.apache.pdfbox.printing.Scaling;
import org.apache.pdfbox.text.PDFTextStripper;
import java.util.Arrays;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.Date;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;

public class JDEQRGeneratorFact {

	public static void main(String[] args) {

		// ******************//
		// *** Variables ***//
		// *****************//
		VarGlobales.fileC = "SetUp/JDEQRGeneratorFact.properties";
		VarGlobales.Config = new Properties();
		String URLAfip = " ";
		File PathArchPDFOrig = new File(" ");
		File PathArchPDFDest = new File(" ");
		File PathArchPDFMail = new File(" ");
		String StringAfip = " ";
		String VarCE = " ";
		String VarNroLegal = " ";
		String UbeName = " ";
		String UbeVersion = " ";
		String UbeUsr = " ";
		String Procesar = " ";
		Connection BDConexion = null;
		Statement BDSentencia = null;
		ResultSet BDResultado = null;

		/* Codifica datos en Base 64 */
		Encoder encoder = Base64.getEncoder();

		// *****************
		// *** Variables ***
		// *****************

		try {
			VarGlobales.Config.load(new FileInputStream(VarGlobales.fileC.trim()));
		} catch (FileNotFoundException e) {
			Date date = new Date();
			System.out
					.println(date.toString() + ": Archivo de configuracion no encontrado " + VarGlobales.fileC.trim());
		} catch (IOException e) {
			e.printStackTrace();
		}
		// Seteo de parametros de configuracion
		String ImprimeLogs = VarGlobales.Config.getProperty("ImprimeLogs");
		String pathIn = VarGlobales.Config.getProperty("entrada");
		String pathOut = VarGlobales.Config.getProperty("salida");
		String pathInCopias = VarGlobales.Config.getProperty("copias");
		String pathInMails = VarGlobales.Config.getProperty("mails");
		String pathInNCD = VarGlobales.Config.getProperty("ncsacI");
		String pathOuNCD = VarGlobales.Config.getProperty("ncsacO");
		String From = VarGlobales.Config.getProperty("MailFrom");
		String Pass = VarGlobales.Config.getProperty("MailPass");
		String pathLeyendo = " ";
		Integer TiempoEspera = Integer.parseInt(VarGlobales.Config.getProperty("tiempoEspera"));
		String Imprimir = VarGlobales.Config.getProperty("imprimir");
		Integer QRPosX = Integer.parseInt(VarGlobales.Config.getProperty("QRPosX"));
		Integer QRPosY = Integer.parseInt(VarGlobales.Config.getProperty("QRPosY"));
		Integer QRAncho = Integer.parseInt(VarGlobales.Config.getProperty("QRAncho"));
		Integer QRAlto = Integer.parseInt(VarGlobales.Config.getProperty("QRAlto"));
		String PathIMGCodQR = VarGlobales.Config.getProperty("QRUbic");
		String URLAfipFijo = VarGlobales.Config.getProperty("URL");
		String UsrBD = VarGlobales.Config.getProperty("UsrBD");
		String PassBD = VarGlobales.Config.getProperty("PassBD");
		String ServerBD = VarGlobales.Config.getProperty("ServerBD");
		String PortBD = VarGlobales.Config.getProperty("PortBD");
		String BaseBD = VarGlobales.Config.getProperty("BaseBD");
		String EnviarMailAdmin = VarGlobales.Config.getProperty("EnviarMailAdmin");
		Integer Lectura = 0;
		String AbrirDocMails = "Y";
		PDDocument PdfParaMails = null;
		String NombreArchivo = " ";

		while (true) {

			if (Lectura == 0) {
				pathLeyendo = pathIn;
			} else {
				if (Lectura == 1) {
					pathLeyendo = pathInCopias;
				} else {
					if (Lectura == 2) {
						pathLeyendo = pathInMails;
					} else {
						if (Lectura == 3) {
							pathLeyendo = pathInNCD;
						}
					}
				}
			}
			if (ImprimeLogs.equals("Y")) {
				Date date = new Date();
				System.out.println(date.toString() + ": Leyendo -> " + pathLeyendo.trim());
			}

			final File folder = new File(pathLeyendo);

			for (final File fileEntry : folder.listFiles()) {
				NombreArchivo = fileEntry.getName();
				PathArchPDFOrig = new File(pathLeyendo.toString() + "\\" + fileEntry.getName());
				if (Lectura == 0) {
					PathArchPDFDest = new File(pathOut.toString() + "\\" + fileEntry.getName());
				}

				if (Lectura == 3) {
					PathArchPDFDest = new File(pathOuNCD.toString() + "\\" + fileEntry.getName());
				}

				if (!fileEntry.exists()) {
					Date date = new Date();
					System.out.println(date.toString() + ": Error de Directorio o Sin Archivos");
				} else {

					try {

						// Loading an existing document
						PDDocument doc = PDDocument.load(PathArchPDFOrig);
						PDDocumentInformation DocInfo = null;
						if (Lectura != 2) {
							// Obtengo Version del reporte y Usuario Generador
							DocInfo = doc.getDocumentInformation();
							String[] Bloques = DocInfo.getKeywords().split(",");
							UbeName = Bloques[0].trim();
							UbeVersion = Bloques[1].trim();
							UbeUsr = DocInfo.getAuthor();
							String Terminacion = PathArchPDFOrig.toString().toUpperCase().substring(
									PathArchPDFOrig.toString().length() - 7, PathArchPDFOrig.toString().length());

							// Versiones a NO Procesar
							if (UbeName.trim().equals("R5576A568")
									&& (UbeVersion.trim().substring(0, 2).equals("FC")
											|| UbeVersion.trim().substring(0, 2).equals("CG"))
									&& Terminacion.trim().equals("OSA.PDF") && Lectura != 2) {
								Procesar = "1";
							} else {
								// Archivo con Copias para Impresion y Envios de Mails
								if (UbeName.trim().equals("R5576A568") && UbeVersion.trim().substring(0, 2).equals("FC")
										&& Terminacion.trim().equals("ACT.PDF")) {
									Procesar = "2";
								} else {
									if (UbeName.trim().equals("R5576A568")
											&& UbeVersion.trim().substring(0, 2).equals("CD")
											&& Terminacion.trim().equals("OSA.PDF")) {
										Procesar = "3";
									} else {
										if (UbeName.trim().equals("R5576A568")
												&& (UbeVersion.trim().substring(0, 2).equals("NC")
														|| UbeVersion.trim().substring(0, 2).equals("ND"))
												&& Terminacion.trim().equals("OSA.PDF")
												|| (UbeVersion.trim().equals("NCBONIF911"))) {
											// Adecuo el tamaño del QR para las Notas de Credito y Debito
											QRAncho = 70;
											QRAlto = 70;
											Procesar = "4";
										} else {
											if (UbeName.trim().equals("R5576A568")
													&& UbeVersion.trim().substring(0, 2).equals("CB")
													&& Terminacion.trim().equals("OSA.PDF")) {
												QRAncho = 70;
												QRAlto = 70;
												Procesar = "5";
											} else {
												if (UbeName.trim().equals("R5576A568")
														&& (UbeVersion.trim().equals("LMARMI02")
																|| UbeVersion.trim().equals("LMARMI")
																|| UbeVersion.trim().equals("LMARMI04"))) {
													QRAncho = 70;
													QRAlto = 70;
													Procesar = "6";
												} else {
													if (UbeVersion.trim().equals("LMCG0908")
															|| UbeVersion.trim().equals("LMFE0908")
															|| UbeVersion.trim().equals("LMIDIOMA10")) {
														QRPosY = 2;
														QRAncho = 70;
														QRAlto = 70;
														Procesar = "6";

													} else {
														if (Lectura == 3) {
															Procesar = "7";
														} else {
															Procesar = "N";
															doc.close();
														}
													}
												}
											}
										}
									}
								}
							}
						}

						if (!Procesar.equals("N")) {

							PDFTextStripper pdfStripper = null;
							pdfStripper = new PDFTextStripper();
							int TotalPaginas = doc.getNumberOfPages();
							int PaginaActual = 0;

							while (PaginaActual < TotalPaginas) {

								pdfStripper.setStartPage(PaginaActual);
								pdfStripper.setEndPage(PaginaActual + 1);
								/* Lectura del PDF y Armado de String para QR */

								// load all lines into a string
								String pages = pdfStripper.getText(doc);
								// split by detecting newline
								String[] lines = pages.split("\r\n|\r|\n");

								int LineaDePDF = 1; // Just to indicate line number
								for (String temp : lines) {

									// Imprime lineas del PDF
									// System.out.println(LineaDePDF + " " + temp);

									if (Procesar.equals("1") && LineaDePDF == 7) {
										// System.out.println(LineaDePDF + " " + temp);
										VarCE = temp.substring(0, 4);
										VarNroLegal = temp.substring(5, 13);
									}
									if (Procesar.equals("2") && LineaDePDF == 7) {
										// System.out.println(LineaDePDF + " " + temp);
										VarCE = temp.substring(0, 4);
										VarNroLegal = temp.substring(5, 13);
									}
									if (Procesar.equals("3") && LineaDePDF == 7) {
										// System.out.println(LineaDePDF + " " + temp);
										VarCE = temp.substring(0, 4);
										VarNroLegal = temp.substring(5, 13);
									}
									if (Procesar.equals("4") && LineaDePDF == 6) {
										// System.out.println(LineaDePDF + " " + temp);
										VarCE = temp.substring(0, 4);
										VarNroLegal = temp.substring(5, 13);
									}
									if (Procesar.equals("5") && LineaDePDF == 6) {
										// System.out.println(LineaDePDF + " " + temp);
										VarCE = temp.substring(0, 4);
										VarNroLegal = temp.substring(5, 13);
									}
									if (Procesar.equals("6") && LineaDePDF == 6) {
										// System.out.println(LineaDePDF + " " + temp);
										VarCE = temp.substring(0, 4);
										VarNroLegal = temp.substring(5, 13);
									}
									if (Procesar.equals("7") && LineaDePDF == 7) {
										// System.out.println(LineaDePDF + " " + temp);
										VarCE = temp.substring(0, 4);
										VarNroLegal = temp.substring(5, 13);
									}

									LineaDePDF++;
								}
								if (ImprimeLogs.equals("Y")) {
									Date date = new Date();
									System.out.println("\n" + date.toString());
									System.out.println("\nProcesnado Archivo " + UbeName.trim() + " - "
											+ UbeVersion.trim() + " - " + UbeUsr.trim() + "\n" + "CE: " + VarCE.trim()
											+ "\n" + "Nro Legal: " + VarNroLegal.trim() + "\n" + "Archivo -> "
											+ PathArchPDFOrig.toString());
								}
								VarGlobales.StringAfipQR = " ";

								// Obtiene Datos de la Factura de la BD
								URLAfip = ConsultaSQL(BDConexion, BDSentencia, BDResultado, StringAfip, URLAfip,
										VarNroLegal, VarCE, encoder, URLAfipFijo, UsrBD, PassBD, ServerBD, PortBD,
										BaseBD);

								if (Lectura != 2) {
									DocInfo.setCustomMetadataValue("CodigoQR-ELM_" + VarCE + "_" + VarNroLegal,
											VarGlobales.StringAfipQR);
								}

								if (URLAfip.equals(null) || URLAfip.equals(" ")) {
									doc.close();
								} else {

									// Genera Imagen con codigo QR para incrustar a la pagina
									GeneradorQR(PathIMGCodQR, URLAfip);

									PDPage page = (PDPage) doc.getPage(PaginaActual);
									PDImageXObject pdImage = PDImageXObject.createFromFile(PathIMGCodQR, doc);

									// creating the PDPageContentStream object
									PDPageContentStream contents = new PDPageContentStream(doc, page, AppendMode.APPEND,
											false);

									// Inserta Imagen en el PDF
									contents.drawImage(pdImage, QRPosX, QRPosY, QRAncho, QRAlto);
									contents.close();

									// Evaluo si debo generar un nuevo pdf para enviar por mail

									if ((VarGlobales.NroVendedor.equals("1355")
											|| VarGlobales.NroVendedor.equals("1620")
											|| VarGlobales.NroCliente.equals("1341001")
											|| VarGlobales.NroCliente.equals("1341001")
											|| VarGlobales.NroCliente.equals("1007001")
											|| VarGlobales.NroCliente.equals("25001001")
											|| VarGlobales.NroCliente.equals("26001001")) && Lectura == 0) {

										if (AbrirDocMails.equals("Y")) {
											PdfParaMails = new PDDocument();
											AbrirDocMails = "N";
										}
										PdfParaMails.addPage(page);

										// naza
									}
									PaginaActual++;
								}

							}

							// Guarda el PDF con el QR Incrustado
							doc.save(PathArchPDFOrig);
							if (AbrirDocMails.equals("N")) {
								PdfParaMails.save(pathInMails + "\\" + NombreArchivo.toString());
								PdfParaMails.close();
								AbrirDocMails = "Y";
							}
							doc.close();

							// Solo Imprimo si no se esta leyendo el directorio de Copias
							if (Lectura == 1) {

								String Impresora = ImpresoraDefault(UbeName.trim(), UbeVersion.trim());
								if (Impresora.isEmpty() || Impresora.equals(" ")) {
									Date date = new Date();
									System.out.println(date.toString() + ": No se ha configurado para la version "
											+ UbeVersion.trim()
											+ " una impresora por defecto en el archivo de Propiedades");
								} else {
									if (Imprimir.equals("Y") && Lectura == 1) {
										// Envia el PDF con el QR a la Impresora
										if (ImprimeLogs.equals("Y")) {
											Date date = new Date();
											System.out.println(
													date.toString() + ": Enviando a Imprimir a " + Impresora.trim());
										}
										Impresor(PathArchPDFOrig, UbeVersion, Impresora, ImprimeLogs);
									}
								}
							}
						}
					} catch (IOException | PrintException e) {
						e.printStackTrace();
					}
					// naza

					if (!Procesar.equals("N") || Lectura == 2) {

						// Procesa Duplicados para Mails
						if (Lectura == 2) {
							PathArchPDFMail = new File(pathLeyendo.toString() + "\\FC_" + VarGlobales.NroCliente.trim()
									+ "_" + VarCE.trim() + "_" + VarNroLegal.trim() + ".pdf");
							PathArchPDFOrig.renameTo(PathArchPDFMail);

							if (VarGlobales.NroVendedor.trim().equals("1620")
									&& VarGlobales.NroCliente.trim().equals("13558001")) {
								EnvioMails(VarGlobales.NroVendedor.trim(), VarGlobales.NroCliente.trim(),
										PathArchPDFMail, From, Pass);
								if (EnviarMailAdmin.equals("Y")) {
									EnvioMails("ADMIN", VarGlobales.NroCliente.trim(), PathArchPDFMail, From, Pass);
								}

							}

							if (VarGlobales.NroVendedor.trim().equals("1355")) {
								EnvioMails(VarGlobales.NroVendedor.trim(), VarGlobales.NroCliente.trim(),
										PathArchPDFMail, From, Pass);
								if (EnviarMailAdmin.equals("Y")) {
									EnvioMails("Admin", VarGlobales.NroCliente.trim(), PathArchPDFMail, From, Pass);
								}
							}

							if (VarGlobales.NroCliente.trim().equals("1341001")) {
								EnvioMails(VarGlobales.NroVendedor.trim(), VarGlobales.NroCliente.trim(),
										PathArchPDFMail, From, Pass);
								if (EnviarMailAdmin.equals("Y")) {
									EnvioMails("Admin", VarGlobales.NroCliente.trim(), PathArchPDFMail, From, Pass);
								}
							}

							if (VarGlobales.NroCliente.trim().equals("1007001")
									|| VarGlobales.NroCliente.trim().equals("25001001")
									|| VarGlobales.NroCliente.trim().equals("26001001")) {
								EnvioMails(VarGlobales.NroVendedor.trim(), VarGlobales.NroCliente.trim(),
										PathArchPDFMail, From, Pass);
								if (EnviarMailAdmin.equals("Y")) {
									EnvioMails("Admin", VarGlobales.NroCliente.trim(), PathArchPDFMail, From, Pass);
								}
							}

						}

						// Procesa Archivos Completos
						if (Lectura == 0) {
							// Muevo El Archivo Procesado
							if (ImprimeLogs.equals("Y")) {
								Date date = new Date();
								System.out.println("\n" + date.toString());
								System.out.println("\nEnviando archivo a -> " + PathArchPDFDest.toString());
							}
							PathArchPDFDest.delete();
							PathArchPDFOrig.renameTo(PathArchPDFDest);
						}

						// Proceso de NCD
						if (Lectura == 3) {
							// Muevo El Archivo Procesado
							if (ImprimeLogs.equals("Y")) {
								Date date = new Date();
								System.out.println("\n" + date.toString());
								System.out.println("\nEnviando archivo a -> " + PathArchPDFDest.toString());
							}
							PathArchPDFDest.delete();
							PathArchPDFOrig.renameTo(PathArchPDFDest);
						}

						// Elimina Copias Impresas o Enviadas por Mails
						if (Lectura == 1) {
							// Muevo El Archivo Procesado
							PathArchPDFOrig.delete();
						}

						if (Lectura == 2) {
							// Muevo El Archivo Procesado
							PathArchPDFMail.delete();
						}

					}

				}

				System.gc();
			} // Fin del Bucle del For

			/* Muevo Archivo Procesado */
			if (folder.list().length == 0) {
				Date date = new Date();
				System.out.println(date.toString() + ": Sin Archivos ");
				try {
					Thread.sleep(TiempoEspera);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} else {
				PathArchPDFOrig.delete();
			}

			if (Lectura == 0) {
				Lectura++;
			} else {
				if (Lectura == 1) {
					Lectura++;
				} else {
					if (Lectura == 2) {
						Lectura++;
					} else {
						if (Lectura == 3) {
							Lectura = 0;
						}
					}
				}
			}

			System.gc();
		} // Fin Del While Infinito

	}

	public static void Impresor(File pathArchPDFOrig, String UbeVersion, String Impresora, String ImprimirLogs)
			throws PrintException {

		PrintService[] ps = PrintServiceLookup.lookupPrintServices(null, null);
		if (ps.length == 0) {
			throw new IllegalStateException("No hay Impresoras Disponibes");
		}
		if (ImprimirLogs.equals("Y")) {
			Date date = new Date();
			System.out.println(date.toString() + ": Impresoras Disponibles : " + Arrays.asList(ps));
		}

		PrintService myService = null;
		for (PrintService printService : ps) {

			/* Seleccion de la Impresora a Utilizar */
			if (printService.getName().equals(Impresora)) {
				myService = printService;
				break;
			}
		}
		try {

			PrinterJob job = PrinterJob.getPrinterJob();
			job.setPrintService(myService);

			FileInputStream fis = new FileInputStream(pathArchPDFOrig);
			PDDocument pdf = PDDocument.load(fis);

			PrintRequestAttributeSet patts = new HashPrintRequestAttributeSet();
			patts.add(Sides.ONE_SIDED);
			patts.add(MediaSizeName.ISO_A4);
			patts.add(new MediaPrintableArea(1, 1, 210 - 2, 297 - 2, MediaPrintableArea.MM));
			PDFPrintable printable = new PDFPrintable(pdf, Scaling.SHRINK_TO_FIT);

			PDFPageable p = new PDFPageable(pdf);
			job.setPageable(p);
			job.setPrintable(printable);
			job.setJobName("Imprimiendo " + pathArchPDFOrig.getName().toString());
			job.print(patts);
			pdf.close();
			fis.close();
			pathArchPDFOrig.delete();

		} catch (PrinterException e) {
			Date date = new Date();
			System.out.println(date.toString() + ": Error al Imprimir en " + myService.getName() + e);
			throw new PrintException("Error al Imprimir", e);

		} catch (IOException e) {
			Date date = new Date();
			System.out.println(date.toString() + ": Error al Cargar el Archivo " + e);
			throw new PrintException("Error al Cargar el Archivo ", e);
		}

	}

	public static void GeneradorQR(String PathIMGCodQR, String URLAfip) {
		// **********************************************************//
		/* Armado de Codigo QR en Archivo de Imagen */
		// **********************************************************//
		int size = 500;
		String ELMfileType = "png";
		File QRCodeImagen = new File(PathIMGCodQR);
		try {
			Map<EncodeHintType, Object> QRHintType = new EnumMap<EncodeHintType, Object>(EncodeHintType.class);
			QRHintType.put(EncodeHintType.CHARACTER_SET, "UTF-8");
			QRHintType.put(EncodeHintType.MARGIN, 1); /* default = 4 */
			QRCodeWriter mYQRCodeWriter = new QRCodeWriter();
			BitMatrix QRBitMatrix = mYQRCodeWriter.encode(URLAfip, BarcodeFormat.QR_CODE, size, size, QRHintType);
			int QRWidth = QRBitMatrix.getWidth();
			BufferedImage QRImage = new BufferedImage(QRWidth, QRWidth, BufferedImage.TYPE_INT_RGB);
			QRImage.createGraphics();
			Graphics2D QRGraphics = (Graphics2D) QRImage.getGraphics();
			QRGraphics.setColor(Color.white);
			QRGraphics.fillRect(0, 0, QRWidth, QRWidth);
			QRGraphics.setColor(Color.black);

			for (int i = 0; i < QRWidth; i++) {
				for (int j = 0; j < QRWidth; j++) {
					if (QRBitMatrix.get(i, j)) {
						QRGraphics.fillRect(i, j, 1, 1);
					}
				}
			}
			ImageIO.write(QRImage, ELMfileType, QRCodeImagen);

		} catch (WriterException e) {
			System.out.println("\nSe ha producido un error\n");
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static String ConsultaSQL(Connection BDConexion, Statement BDSentencia, ResultSet BDResultado,
			String StringAfip, String URLAfip, String VarNroLegal, String VarCE, Encoder encoder, String URLAfipFijo,
			String UsrBD, String PassBD, String ServerBD, String PortBD, String BaseBD) {

		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
		} catch (ClassNotFoundException exception) {
			System.out.println("No se encontro el Diver JDBC para Oracle: " + exception.toString());
			return " ";
		}

		if (BDConexion == null) {
			DriverManager.setLoginTimeout(5);
			try {
				BDConexion = DriverManager.getConnection("jdbc:oracle:thin:@" + ServerBD + ":" + PortBD + ":" + BaseBD,
						UsrBD, PassBD);
			} catch (SQLException e) {
				System.out.println("Error en la conexion, revise los logs");
				e.printStackTrace();
				return " ";
			}
		}
		if (BDSentencia == null) {
			try {
				BDSentencia = BDConexion.createStatement();
			} catch (SQLException e) {
				e.printStackTrace();
				return " ";

			}
		}
		try {
			String Query = "select \r\n"
					+ "to_char(compartido.fechag(hfdivj),'YYYY')||'-'||to_char(compartido.fechag(hfdivj),'MM')||'-'||to_char(compartido.fechag(hfdivj),'DD') fecha,\r\n"
					+ "REPLACE(HFTAX,'-','') CUIT,\r\n" + "HFACEM,\r\n" + "substr(ttvinv,0,2), \r\n" + "HFAINW,\r\n"
					+ "abs(HFAG) IMPORTE,\r\n" + "SUBSTR(HFDL02,0,3) MONEDA,\r\n" + "hfcrr cotizacion,\r\n"
					+ "REPLACE(HFTAXX,'-','')cuitcliente,\r\n" + "hfacai, shslsm, hfan8, hfagrp \r\n"
					+ "from proddta.f76a0209, proddta.f76a09, proddta.f42150 where hfdoco = shdoco (+) and hfdcto = shdcto (+) and "
					+ "hfacem = '" + VarCE.trim() + "' and\r\n" + "hfainw = '" + VarNroLegal.trim()
					+ "' and hfdoc = ttdoc and hfdct = ttdct ";
			// System.out.println("Ejecutando: "+Query);
			BDResultado = BDSentencia.executeQuery(Query);

		} catch (SQLException e1) {
			e1.printStackTrace();
			return " ";

		}
		try {
			if (BDResultado.next()) {

				StringAfip = "{\"ver\":1,\"fecha\":\"";
				StringAfip = StringAfip + BDResultado.getString(1);
				StringAfip = StringAfip + "\",\"cuit\":" + BDResultado.getString(2).trim() + ",";
				StringAfip = StringAfip + "\"ptoVta\":" + BDResultado.getString(3).trim() + ",";
				StringAfip = StringAfip + "\"tipoCmp\":" + BDResultado.getString(4).trim() + ",";
				StringAfip = StringAfip + "\"nroCmp\":" + BDResultado.getString(5).trim() + ",";
				StringAfip = StringAfip + "\"importe\":" + BDResultado.getString(6).trim() + ",";
				StringAfip = StringAfip + "\"moneda\":\"" + BDResultado.getString(7).trim() + "\",";
				StringAfip = StringAfip + "\"ctz\":" + BDResultado.getString(8).trim() + ",";
				StringAfip = StringAfip + "\"tipoDocRec\":" + "80" + ",";
				StringAfip = StringAfip + "\"nroDocRec\":" + BDResultado.getString(9).trim() + ",";

				if (BDResultado.getString(13).trim().equals("E")) {
					StringAfip = StringAfip + "\"tipoCodAut\":" + "\"E" + "\",";
				} else {
					StringAfip = StringAfip + "\"tipoCodAut\":" + "\"A" + "\",";
				}

				StringAfip = StringAfip + "\"codAut\":" + BDResultado.getString(10).trim() + "}";
				// System.out.println(StringAfip);

				if (BDResultado.getString(11) == null) {
					VarGlobales.NroVendedor = " ";
				} else {
					VarGlobales.NroVendedor = BDResultado.getString(11).trim();
				}

				VarGlobales.NroCliente = BDResultado.getString(12).trim();

				// Codifico en Base64
				String encodedString = encoder.encodeToString(StringAfip.getBytes());
				URLAfip = URLAfipFijo + encodedString;
				VarGlobales.StringAfipQR = URLAfipFijo.trim() + StringAfip.trim();

				return URLAfip;

			} else {
				throw new SQLException("No se encontro la Facura en la BD " + VarCE.trim() + " " + VarNroLegal.trim());
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return " ";
		}

	}

	public static void EnvioMails(String NroVendedor, String NroCliente, File PathArchPDFOrig, String From,
			String Pass) {

		String to = " ";// Destinatario
		String Smtp = VarGlobales.Config.getProperty("SMTP");

		if (NroVendedor.equals("1620")) {
			to = "mmonjes@lasmarias.com.ar";
		}

		if (NroVendedor.equals("1355")) {
			to = "facturasbogari@gmail.com";

		}

		if (NroCliente.equals("1007001") || NroCliente.equals("25001001") || NroCliente.equals("26001001")) {
			to = "facturasedi@cencosud.com.ar";
		}

		if (NroCliente.equals("1341001")) {
			to = "FacturaElectronica@nini.com.ar";
		}

		if (NroVendedor.toUpperCase().equals("ADMIN")) {
			to = VarGlobales.Config.getProperty("MailAdmin");
		}

		final String user = From;// Originador del Mail
		Properties properties = System.getProperties();
		properties.setProperty("mail.smtp.host", Smtp); // Direccion del SMTP
		properties.put("mail.smtp.auth", "false");
		Session session = Session.getInstance(properties);

		try {
			MimeMessage message = new MimeMessage(session);
			message.setFrom(new InternetAddress(user));
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
			message.setSubject("Factura Generada por Establecimiento Las Marias");
			BodyPart messageBodyPart1 = new MimeBodyPart();
			messageBodyPart1.setText("Se Adjunta la Factura Generada Electronicamente");

			MimeBodyPart messageBodyPart2 = new MimeBodyPart();
			DataSource source = new FileDataSource(PathArchPDFOrig);
			messageBodyPart2.setDataHandler(new DataHandler(source));

			messageBodyPart2.setFileName(PathArchPDFOrig.getName());

			Multipart multipart = new MimeMultipart();
			multipart.addBodyPart(messageBodyPart1);
			multipart.addBodyPart(messageBodyPart2);
			message.setContent(multipart);

			Transport.send(message);
			if (NroVendedor.equals("1355")) {
				to = "jmarzano@lasmarias.com.ar";
				Transport.send(message);
			}
			if (NroCliente.equals("1341001")) {
				to = "ehernandez@lasmarias.com.ar";
				Transport.send(message);
			}
		} catch (MessagingException ex) {
			ex.printStackTrace();
		}
	}

	public static class VarGlobales {
		public static String NroVendedor = " ";
		public static String NroCliente = " ";
		public static String StringAfipQR = " ";
		public static String fileC = " ";
		public static String Smtp = " ";
		public static Properties Config = new Properties();

	}

	public static String ImpresoraDefault(String Reporte, String Version) {
		try {
			VarGlobales.Config.load(new FileInputStream(VarGlobales.fileC.trim()));
		} catch (FileNotFoundException e) {
			System.out.println("Archivo de configuracion no encontrado " + VarGlobales.fileC.trim());
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Seteo de parametros de configuracion
		String Impresora = VarGlobales.Config.getProperty(Version.trim());
		if (Impresora == null) {
			Impresora = " ";
		}
		return Impresora;
	}

}
