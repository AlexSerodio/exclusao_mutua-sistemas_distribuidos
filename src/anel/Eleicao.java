package anel;

import java.util.LinkedList;

public class Eleicao {
	
	public Processo realizarEleicao(int idProcessoIniciador) {
		//System.out.println("Processo de eleicao iniciado");
		
		LinkedList<Integer> idProcessosConsultados = new LinkedList<>();
		for (Processo p : Anel.processosAtivos)
			consultarProcesso(p.getPid(), idProcessosConsultados);
		
		int idNovoCoordenador = idProcessoIniciador;
		for (Integer id : idProcessosConsultados) {
			if (id > idNovoCoordenador)
				idNovoCoordenador = id;
		}
		
		Processo coordenador = atualizarCoordenador(idNovoCoordenador);
		
		System.out.println("Eleicao concluida com sucesso. O novo coordenador eh " + idNovoCoordenador + ".");
		return coordenador;
	}
	
	private void consultarProcesso(int idProcesso, LinkedList<Integer> processosConsultados) {
		processosConsultados.add(idProcesso);
	}
	
	private Processo atualizarCoordenador(int idNovoCoordenador) {
		Processo coordenador = null;
		for (Processo p : Anel.processosAtivos) {			
			if (p.getPid() == idNovoCoordenador) {
				p.setEhCoordenador(true);
				coordenador = p;
			} else {
				p.setEhCoordenador(false);
			}
		}
		
		return coordenador;
	}
	
}