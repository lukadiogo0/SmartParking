package ipca.djpm.smartparking.ui.home.ui.home.contactos

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import android.widget.BaseAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.floatingactionbutton.FloatingActionButton
import ipca.djpm.smartparking.DatabaseHelper
import ipca.djpm.smartparking.R
import ipca.djpm.smartparking.Contacto
import ipca.djpm.smartparking.databinding.FragmentContactosBinding


class ContactosFragment : Fragment() {
    private var _binding: FragmentContactosBinding? = null
    private val binding get() = _binding!!
    private var userID : Int? = null

    var contactos = arrayListOf<Contacto>()

    val adapter = ContactoAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentContactosBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val sharedPreferences = requireContext().getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE)
        userID = sharedPreferences.getInt("USER_ID", -1)

        val progressBar = binding.progressBar
        val textViewSemContactos = binding.textViewSemContactos
        val floatingActionButtonAddContacto = binding.floatingActionButtonAddContactos

        Handler(Looper.getMainLooper()).postDelayed(
            {
                if(userID != -1){
                    val query = "SELECT ContactoAluno.numero, TipoContacto.descricao FROM ContactoAluno JOIN Aluno_Utilizador ON Aluno_Utilizador.numAluno = ContactoAluno.numAluno JOIN TipoContacto ON TipoContacto.tipoContactoID = ContactoAluno.tipoContactoID WHERE utilizadorID = ${userID}"
                    val databaseHelper = DatabaseHelper()
                    val result = context?.let { databaseHelper.selectQuery(query, it) }
                    if (result != null) {
                        while(result.next()){
                            val numero = result.getInt("numero")
                            var tipoContacto = result.getString("descricao")
                            tipoContacto = tipoContacto.replace("\\s+".toRegex(), "")
                            val contacto = Contacto(numero, tipoContacto)
                            contactos.add(contacto)
                        }
                        progressBar.visibility = View.INVISIBLE

                        if(contactos.size == 0){
                            textViewSemContactos.visibility = View.VISIBLE
                        }
                        adapter.notifyDataSetChanged()
                    }
                }
            }, 1)


        floatingActionButtonAddContacto.setOnClickListener{
            findNavController().navigate(R.id.action_navigation_contactos_to_contactosFragmentAdd)
        }
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)

        binding.listViewContactos.adapter = adapter

    }

    override fun onDestroyView() {
        super.onDestroyView()
        contactos.clear()
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

    inner class ContactoAdapter : BaseAdapter() {
        override fun getCount(): Int {
            return contactos.size
        }

        override fun getItem(position: Int): Any {
            return contactos[position]
        }

        override fun getItemId(position: Int): Long {
            return 0
        }

        @RequiresApi(Build.VERSION_CODES.M)
        override fun getView(position: Int, view: View?, parent: ViewGroup?): View {
            val rootView = layoutInflater.inflate(R.layout.row_contactos,parent,false)

            val textViewNumero = rootView.findViewById<TextView>(R.id.textViewNumero)
            val textViewTipoContacto = rootView.findViewById<TextView>(R.id.textViewTipoContacto)
            val floatingActionButtonDeleteContacto = rootView.findViewById<FloatingActionButton>(R.id.floatingActionButtonDeleteContacto)
            val progressBar = binding.progressBar

            textViewNumero.text = contactos[position].numero.toString()
            textViewTipoContacto.text = contactos[position].tipocontacto

            floatingActionButtonDeleteContacto.setOnClickListener{
                progressBar.visibility = View.VISIBLE
                Handler(Looper.getMainLooper()).postDelayed(
                    {
                        if (userID != -1) {
                            val databaseHelper = DatabaseHelper()
                                val query = "Delete contactoAluno FROM ContactoAluno Join Aluno_Utilizador on Aluno_Utilizador.numAluno = ContactoAluno.numAluno Where numero = ${contactos[position].numero} AND utilizadorID = ${userID}"
                                val result = context?.let { databaseHelper.executeQuery(query, it) }!!
                                if (result) {
                                    Toast.makeText(context, "Contacto removido com sucesso", Toast.LENGTH_SHORT).show()
                                    progressBar.visibility = View.INVISIBLE
                                    contactos.remove(contactos[position])
                                    adapter.notifyDataSetChanged()
                                } else {
                                    Toast.makeText(context, "Erro ao remover contacto", Toast.LENGTH_SHORT).show()
                                    progressBar.visibility = View.INVISIBLE
                                }
                            }
                    }, 1)
            }

            return rootView
        }
    }
}