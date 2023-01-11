package ipca.djpm.smartparking.ui.home.ui.home

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import ipca.djpm.smartparking.DatabaseHelper
import ipca.djpm.smartparking.databinding.FragmentVeiculosAddBinding
import java.util.regex.Pattern

class VeiculosFragmentAdd: Fragment() {
    private var _binding: FragmentVeiculosAddBinding? = null
    private val binding get() = _binding!!
    private var userID: Int? = null
    private var tipoVeiculoID: Int = -1
    private var arrayTipoVeiculos = arrayListOf<String>()
    private var arrayMatriculas = arrayListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentVeiculosAddBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val sharedPreferences = requireContext().getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE)
        userID = sharedPreferences.getInt("USER_ID", -1)

        val progressBar = binding.progressBarAddVeiculo
        val spinnerTipoVeiculo = binding.spinnerTipoVeiculo
        val buttonAdd = binding.buttonAdd
        val textViewNovoVeiculo = binding.textViewNovoVeiculo
        val textViewAddTipoVeiculo = binding.textViewAddTipoVeiculo
        val editTextAddVeiculoMatricula = binding.editTextAddVeiculoMatricula
        val textViewAddMatricula = binding.textViewAddMatricula

        Handler(Looper.getMainLooper()).postDelayed(
            {
                var query = "SELECT descricao FROM TipoVeiculo ORDER BY tipoVeiculoID"
                val databaseHelper = DatabaseHelper()
                var result = context?.let { databaseHelper.selectQuery(query, it) }
                if (result != null) {
                    while (result.next()) {
                        var descricao = result.getString("descricao")
                        descricao = descricao.replace("\\s+".toRegex(), "")
                        arrayTipoVeiculos.add(descricao)
                    }
                    val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, arrayTipoVeiculos)
                    spinnerTipoVeiculo.adapter = adapter

                    query = "SELECT matricula FROM Veiculo"
                    result = context?.let { databaseHelper.selectQuery(query, it) }
                    if (result != null){
                        while (result.next()) {
                            var matricula = result.getString("matricula")
                            matricula = matricula.replace("\\s+".toRegex(), "")
                            arrayMatriculas.add(matricula)
                        }
                        progressBar.visibility = View.INVISIBLE
                        spinnerTipoVeiculo.visibility = View.VISIBLE
                        buttonAdd.visibility = View.VISIBLE
                        textViewNovoVeiculo.visibility = View.VISIBLE
                        textViewAddTipoVeiculo.visibility = View.VISIBLE
                        editTextAddVeiculoMatricula.visibility = View.VISIBLE
                        textViewAddMatricula.visibility = View.VISIBLE
                    }
                    else{
                        Toast.makeText(context, "Erro ao obter dados de veículos", Toast.LENGTH_SHORT).show()
                    }
                }else{
                    Toast.makeText(context, "Erro ao obter dados de veículos", Toast.LENGTH_SHORT).show()
                }
            }, 1)

        spinnerTipoVeiculo.onItemSelectedListener = object :
           AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>,view: View, position: Int, id: Long) {
                tipoVeiculoID = position+1
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
                tipoVeiculoID = -1
            }
           }

        buttonAdd.setOnClickListener{
            var matricula = editTextAddVeiculoMatricula.text.toString()
            if(matricula.isNotBlank()) {
                matricula = matricula.uppercase()
                if(tipoVeiculoID != -1 && verificarMatricula(matricula)){
                    val databaseHelper = DatabaseHelper()
                    if(!arrayMatriculas.contains(matricula)){
                        val query = "INSERT INTO Veiculo(matricula, tipoVeiculoID) VALUES('${matricula}', ${tipoVeiculoID})"
                        var result = context?.let { databaseHelper.executeQuery(query, it) }
                        if (result == true) {
                            val query = "INSERT INTO Utilizador_Veiculo(utilizadorID, matricula) VALUES(${userID}, '${matricula}')"
                            result = context?.let { databaseHelper.executeQuery(query, it) }
                            if (result == true) {
                                Toast.makeText(context, "Veículo adicionado com sucesso", Toast.LENGTH_SHORT).show()
                                findNavController().popBackStack()
                            }else{
                                Toast.makeText(context, "Erro ao adicionar veículo", Toast.LENGTH_SHORT).show()
                            }
                        }else{
                            Toast.makeText(context, "Erro ao adicionar veículo", Toast.LENGTH_SHORT).show()
                        }
                    }else{
                        Toast.makeText(context, "Veículo já existente", Toast.LENGTH_SHORT).show()
                        val query = "INSERT INTO Utilizador_Veiculo(utilizadorID, matricula) VALUES(${userID}, '${matricula}')"
                        val result = context?.let { databaseHelper.executeQuery(query, it) }
                        if (result == true) {
                            Toast.makeText(context, "Veículo adicionado com sucesso", Toast.LENGTH_SHORT).show()
                            findNavController().popBackStack()
                        }else{
                            Toast.makeText(context, "Erro ao adicionar veículo", Toast.LENGTH_SHORT).show()
                        }
                    }
                }else{
                    Toast.makeText(context, "Matrícula e/ou tipo de veículo inválido(s)", Toast.LENGTH_SHORT).show()
                }
            }else{
                Toast.makeText(context, "Matrícula não pode esta vazia", Toast.LENGTH_SHORT).show()
            }

        }
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            android.R.id.home -> {
                findNavController().popBackStack()
                true
            }
            else ->{
                false
            }
        }
    }

    private fun verificarMatricula(matricula : String) : Boolean{
        val patterb = Pattern.compile("^(([A-Z]{2}-\\d{2}-(\\d{2}|[A-Z]{2}))|(\\d{2}-(\\d{2}-[A-Z]{2}|[A-Z]{2}-\\d{2})))\$")
        val match = patterb.matcher(matricula)
        return match.matches()
    }
}