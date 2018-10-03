package anel;

import java.util.LinkedList;
import java.util.Random;

/**
 * 
 * @author Alex Serodio Goncalves e Luma Kuhl
 *
 */
public class Processo {
	
	private int pid;
	private Thread utilizaRecurso;
	//private Conexao conexao = new Conexao();
	
	private boolean ehCoordenador;
	private LinkedList<Processo> listaDeEspera;
	private boolean recursoEmUso;
	
//	private static final int USO_PROCESSO_MIN = 5000;
//	private static final int USO_PROCESSO_MAX = 15000;
	private static final int USO_PROCESSO_MIN = 10000;
	private static final int USO_PROCESSO_MAX = 20000;
	
	public Processo(int pid) {
		this.pid = pid;
		setEhCoordenador(false);
	}
	
	public Processo(int pid, boolean ehCoordenador) {
		this.pid = pid;
		setEhCoordenador(ehCoordenador);
	}
	
	public int getPid() {
		return pid;
	}
	
	public boolean isEhCoordenador() {
		return ehCoordenador;
	}

	public void setEhCoordenador(boolean ehCoordenador) {
		this.ehCoordenador = ehCoordenador;
		if(this.ehCoordenador) {
			listaDeEspera = new LinkedList<>();
			//conexao.ativarServidor();
			
			Processo consumidor = RecursoCompartilhado.getConsumidor();
			if(consumidor != null)
				consumidor.interronperAcessoRecurso();
			
			recursoEmUso = false;
		}
	}
	
	public boolean isRecursoEmUso() {
		return encontrarCoordenador().recursoEmUso;
	}
	
	public void setRecursoEmUso(boolean estaEmUso, Processo processo) {
		Processo coordenador = encontrarCoordenador();
		
		coordenador.recursoEmUso = estaEmUso;
		RecursoCompartilhado.setConsumidor(coordenador.recursoEmUso ? processo : null);
	}
	
	public boolean isListaDeEsperaEmpty() {
		return getListaDeEspera().isEmpty();
	}
	
	private void removerDaListaDeEspera(Processo processo) {
		if(getListaDeEspera().contains(processo))
			getListaDeEspera().remove(processo);
	}
	
	private LinkedList<Processo> getListaDeEspera() {
		return encontrarCoordenador().listaDeEspera;
	}
	
	private Processo encontrarCoordenador() {
		Processo coordenador = null;
		for (Processo p : Anel.processosAtivos) {
			if (p.isEhCoordenador()) {
				coordenador = p;
				break;
			}
		}
		
		if(coordenador == null) {
			coordenador = comecarEleicao();
			System.out.println("Eleicao concluida com sucesso. O novo coordenador eh " + coordenador + ".");
		}
		return coordenador;
	}

	private Processo comecarEleicao() {
		Eleicao eleicao = new Eleicao();
		Processo coordenador = eleicao.realizarEleicao(getPid());
			
		try {
			if(coordenador.equals(RecursoCompartilhado.getConsumidor()))
				coordenador.interronperAcessoRecurso();
		} catch (NullPointerException e) {
			comecarEleicao();
		}
		return coordenador;
	}
	
	public void acessarRecursoCompartilhado() {
		if(RecursoCompartilhado.isUsandoRecurso(this) || this.isEhCoordenador())
			return;
		
		if(isRecursoEmUso())
			adicionarNaListaDeEspera(this);
		else
			utilizarRecurso(this);
	}
	
	public void adicionarNaListaDeEspera(Processo processoEmEspera) {
		getListaDeEspera().add(processoEmEspera);
		
		System.out.println("Processo " + this + " foi adicionado na lista de espera.");
		System.out.println("Lista de espera: " + getListaDeEspera());
	}
	
	private void utilizarRecurso(Processo processo) {
		Random random = new Random();
		int randomUsageTime = USO_PROCESSO_MIN + random.nextInt(USO_PROCESSO_MAX - USO_PROCESSO_MIN);
		
		utilizaRecurso = new Thread(new Runnable() {
			@Override
			public void run() {
				System.out.println("Processo " + processo + " está consumindo o recurso por " + randomUsageTime + " ms.");
				setRecursoEmUso(true, processo);
				try {
					Thread.sleep(randomUsageTime);
				} catch (InterruptedException e) { }
				
				System.out.println("Processo " + processo + " parou de consumir o recurso.");
				processo.liberarRecurso();
			}
		});
		utilizaRecurso.start();
	}
	
	private void liberarRecurso() {
		setRecursoEmUso(false, this);
		
		if(!isListaDeEsperaEmpty()) {
			Processo processoEmEspera = getListaDeEspera().removeFirst();
			processoEmEspera.acessarRecursoCompartilhado();
			System.out.println("Processo " + processoEmEspera + " foi removido da lista de espera.");
			System.out.println("Lista de espera: " + getListaDeEspera());
		}
	}
	
	private void interronperAcessoRecurso() {
		if(utilizaRecurso != null)
			utilizaRecurso.interrupt();
	}
	
	public void destruir() {
		if(RecursoCompartilhado.isUsandoRecurso(this)) {
			interronperAcessoRecurso();
			liberarRecurso();
		}
		
		if(!this.isEhCoordenador())
			removerDaListaDeEspera(this);
		
		Anel.processosAtivos.remove(this);
	}
	
	@Override
	public boolean equals(Object obj) {
		Processo processo = (Processo) obj;
		if(processo == null)
			return false;
		
		return this.pid == processo.pid;
	}
	
	@Override
	public String toString() {
		return String.valueOf(this.getPid());
	}
}