package com.grt.pokemon.ui.profile

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Spinner
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.grt.pokemon.R
import com.grt.pokemon.common.BaseFragment
import com.grt.pokemon.databinding.FragmentProfileBinding
import com.grt.pokemon.domain.model.PokemonModel
import com.grt.pokemon.ui.dialog.DialogFragment
import com.grt.pokemon.ui.home.HomeViewModel
import com.grt.pokemon.ui.superfavorito.SuperFavoritoFragment
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File


/**
 * Created por Gema Rosas Trujillo
 * 28/01/2022
 * Clase encargada de la opción de Menu Perfil, en ella se persisten 4 datos, uno de los cuales
 * te permite seleccionar un pokemon super favorito de un spinner, cuya imagen será mostrada
 * en la opción de menu de Tu Pokemon Super Favorito
 */
class ProfileFragment : BaseFragment<FragmentProfileBinding, ProfileViewModel>() {

    private val dialogMessage by lazy { DialogFragment.newInstance() }

    override val vm: ProfileViewModel by viewModel()

    val vmHome: HomeViewModel by sharedViewModel()

    private lateinit var spinner : Spinner

    override fun provideBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentProfileBinding {
        return FragmentProfileBinding.inflate(inflater,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupBinding()

        vm.onInit()
        observeData(vm.obsName,::onObserveName)
        observeData(vm.obsSurname,::onObserveSurname)
        observeData(vm.obsEmail,::onObserveEmail)
        observeData(vm.obsNamePokSuperStar,::onObservePokStar)
    }

    override fun onResume() {
        vm.onShowFab(false)
        super.onResume()
    }

    private fun onObserveName(name: String) {
        binding.etName.setText(name)
    }

    private fun onObserveSurname(surname: String) {
        binding.etSurname.setText(surname)
    }

    private fun onObserveEmail(email: String) {
        binding.etEmail.setText(email)
    }

    private fun onObservePokStar(star: String) {
        spinner = binding.spPokemons
        val idPos = if (star == "") 0  else star.toInt() as Int
        spinner.setSelection(idPos, true)

    }

    private fun setupBinding() {

        with(binding) {

            val listPokemon = vmHome.obsListPokemons.value
            if (!listPokemon.isNullOrEmpty()) {

                spPokemons.adapter = vmHome.obsListPokemons.value?.let {
                    val listaSpinner = listPokemon.sortedBy {
                        it.name
                    }
                    SpinnerPokemonAdapter(requireContext(), R.layout.spinner_pokemons, listaSpinner, binding)
                }

                // Evento del Spinner
                spPokemons.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(adapterView: AdapterView<*>?, view: View, i: Int, l: Long) {
                        val pokemonModel = spPokemons.selectedItem as PokemonModel
                        val imgFile = File(pokemonModel.url_image_default)
                        if (imgFile.exists()) {
                            val myBitmap = BitmapFactory.decodeFile(imgFile.path)
                            ivPokSSStar.setImageBitmap(myBitmap)
                        }
                    }

                    override fun onNothingSelected(adapterView: AdapterView<*>?) {
                        return
                    }
                })


                // Evento del botón Crear Perfil
                btnCrearPerfil.setOnClickListener {
                    lifecycleScope.launch {

                        val name = etName.text.toString()
                        val surname = etSurname.text.toString()
                        val email = etEmail.text.toString()
                        val pokemonSeleccionadoIdPosition =
                            spPokemons.selectedItemPosition.toString()
                        val pokemonModel = spPokemons.selectedItem as PokemonModel

                        vm.onActionSaveProfileClick(
                            name,
                            surname,
                            email,
                            pokemonSeleccionadoIdPosition,
                            pokemonModel.id.toString()
                        )
                    }
                }
            } else {
                // Si se intenta entrar en esta opción pero aún no se ha descargado la lista de Pokemons,
                // se muestra un mensaje indicándole que antes a de iniciar la aplicación.
                // En esta pestaña se persiste un Spinner para que el usuario almacene en su Perfil
                // su Pokemon Super Favorito y por ello este mensaje
                dialogMessage.show(parentFragmentManager, SuperFavoritoFragment::class.java.name, "Aún no ha descargado la Lista de Pokemons"){
                    findNavController().navigate(ProfileFragmentDirections.actionNavProfileToNavHome())
                    dialogMessage.dismiss(parentFragmentManager)
                }
            }
        }
    }
}