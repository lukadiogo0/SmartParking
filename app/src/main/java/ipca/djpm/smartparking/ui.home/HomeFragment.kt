package ipca.djpm.smartparking.ui.home

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import ipca.djpm.smartparking.DatabaseHelper
import ipca.djpm.smartparking.R
import ipca.djpm.smartparking.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private var userID : Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val sharedPreferences = requireContext().getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE)
        userID = sharedPreferences.getInt("USER_ID", -1)
        val buttonVeiculos = binding.buttonVeiculos
        val buttonEntrarSair = binding.buttonEntrarSair
        val textViewBoasVindas = binding.textViewBoasVindas
        val progressBar = binding.progressBarHome
        val textViewLugar = binding.textViewLugar
        progressBar.visibility = View.VISIBLE
        val databaseHelper = DatabaseHelper()
        Handler(Looper.getMainLooper()).postDelayed(
            {
                if(userID != -1) {
                    var query = "SELECT nome FROM Aluno JOIN Aluno_Utilizador ON Aluno.numAluno=Aluno_Utilizador.numAluno JOIN Utilizador ON Aluno_Utilizador.utilizadorID=Utilizador.utilizadorID WHERE Utilizador.utilizadorID=${userID}"
                    var resultQuery = context?.let { databaseHelper.selectQuery(query, it) }
                    if (resultQuery != null) {
                        if (resultQuery.next()) {
                            var nome = resultQuery.getString("nome")
                            nome = nome.replace("\\s+".toRegex(), " ")
                            textViewBoasVindas.text = "Ol??, ${nome}"
                        }
                    }else {
                        Toast.makeText(context, "Erro ao obter nome", Toast.LENGTH_SHORT).show()
                        progressBar.visibility = View.INVISIBLE
                    }

                    query = "SELECT nomeEscola, numLugar FROM Utilizador JOIN Escola ON Utilizador.escolaID=Escola.escolaID WHERE Utilizador.utilizadorID=${userID}"
                    resultQuery = context?.let { databaseHelper.selectQuery(query, it) }
                    if (resultQuery != null) {
                        if (resultQuery.next()) {
                            var nomeEscola = resultQuery.getString("nomeEscola")
                            var numLugar = resultQuery.getString("numLugar")
                            nomeEscola = nomeEscola.replace("\\s+".toRegex(), "")
                            numLugar = numLugar.replace("\\s+".toRegex(), "")
                            textViewLugar.text = "Lugar: ${nomeEscola} - ${numLugar}"
                        }
                    }else {
                        Toast.makeText(context, "Erro ao obter lugar", Toast.LENGTH_SHORT).show()
                        progressBar.visibility = View.INVISIBLE
                    }

                    progressBar.visibility = View.INVISIBLE
                    buttonVeiculos.visibility = View.VISIBLE
                    buttonEntrarSair.visibility = View.VISIBLE
                    textViewBoasVindas.visibility = View.VISIBLE
                    textViewLugar.visibility = View.VISIBLE
                }
            }, 100)

        buttonVeiculos.setOnClickListener{
            findNavController().navigate(R.id.action_navigation_home_to_navigation_veiculos)
        }

        buttonEntrarSair.setOnClickListener{
            progressBar.visibility = View.VISIBLE
            Handler(Looper.getMainLooper()).postDelayed(
                {
                    if(userID != -1) {
                        var query = "SELECT ocupado FROM Estacionamento JOIN Utilizador on Utilizador.numLugar=Estacionamento.numLugar AND Utilizador.escolaID=Estacionamento.escolaID WHERE Utilizador.utilizadorID=${userID}"
                        val resultQuery = context?.let { databaseHelper.selectQuery(query, it) }
                        if (resultQuery != null) {
                            if (resultQuery.next()) {
                                var ocupado = resultQuery.getInt("ocupado")
                                if (ocupado == 1){
                                    ocupado = 0
                                }else{
                                    ocupado = 1
                                }
                                query = "UPDATE Estacionamento SET Estacionamento.ocupado=${ocupado} FROM Estacionamento INNER JOIN Utilizador Util on Util.numLugar=Estacionamento.numLugar AND Util.escolaID=Estacionamento.escolaID WHERE Util.utilizadorID=${userID}"
                                val result = context?.let { databaseHelper.executeQuery(query, it) }
                                if (result == true){
                                    if(ocupado == 1){
                                        Toast.makeText(context, "Lugar marcado como ocupado", Toast.LENGTH_SHORT).show()
                                    }else{
                                        Toast.makeText(context, "Lugar marcado como livre", Toast.LENGTH_SHORT).show()
                                    }
                                }else{
                                    Toast.makeText(context, "Erro ao alterar ocupa????o de lugar", Toast.LENGTH_SHORT).show()
                                }
                            }else{
                                Toast.makeText(context, "Erro ao obter ocupa????o de lugar", Toast.LENGTH_SHORT).show()
                            }
                        }
                        else{
                            Toast.makeText(context, "Erro ao alterar ocupa????o de lugar", Toast.LENGTH_SHORT).show()
                        }
                        progressBar.visibility = View.INVISIBLE
                    }
                }, 1)
        }
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}