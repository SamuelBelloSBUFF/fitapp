package helenocampos.github.io.fitapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import helenocampos.github.io.fitapp.model.ImcGenderResult;
import helenocampos.github.io.fitapp.model.ImcResult;
import helenocampos.github.io.fitapp.service.GenderService;

@Controller
public class ImcController {

    GenderService genderService;

    @Autowired
    public ImcController(GenderService genderService) {
        this.genderService = genderService;
    }

    @GetMapping("/imc")
    public String imcForm() {
        return "imcForm";
    }

    @GetMapping("/imcGender")
    public String imcGenderForm() {
        return "imcGenderForm";
    }

    /**
     * 
     * Método que realiza a request para o formulário de imc.
     * 
     * @param peso  Peso do usário
     * @param altura    Altura do usuário
     * @param model Model do usuário
     * @return  Retornará o formulário de Imc
     */
    @PostMapping("/imc")
    public String proccessImcRequest(@RequestParam("peso") double peso,
            @RequestParam("altura") double altura,
            Model model) {

        ImcResult resultado = proccessImc(peso, altura);

        model.addAttribute("imc", resultado.getImc());
        model.addAttribute("resultado", resultado.getClassification());

        return "imcForm";
    }

    /**
     * Método que calcula o Imc
     * 
     * @param peso Peso do usuário
     * @param altura Altura do usuário
     * @return Retornará a classificação da tabela imc e o Imc do usuário.
     */
    public ImcResult proccessImc(double peso, double altura) {
        double imc = this.getIMC(peso, altura);
        String resultado = this.getIMCClassification(imc);
        return new ImcResult(resultado, imc);
    }

    /**
     * 
     * Processa o Imc baseado no genero do usuário, processo parte da identificação do gênero a partir do nome do usuário.
     * 
     * @param peso  Peso do usuário
     * @param altura    Altura do usuário
     * @param nome  Nome do usuário
     * @return  Retornará a classificação da tabela imc, o imc do usuário, gênero do usuário e o endereço url onde API buscou o nome.
     */
    public ImcGenderResult proccessImcGender(double peso, double altura, String nome) {
        double imc = this.getIMC(peso, altura);
        String gender = genderService.getGenderByName(nome);
        String resultado = this.getIMCClassificationByGender(imc, gender);
        String genderURL = genderService.getExternalServiceUrl() + nome;
        return new ImcGenderResult(resultado, imc, gender, genderURL);
    }

    /**
     * 
     * Método que processa a requisição do usuário e persiste seu gênero, altura e nome em uma model.
     * 
     * @param peso  Peso do usuário
     * @param altura    Altura do usuário
     * @param nome  Nome do usuário
     * @param model Model do usuário
     * @return  Retornará a classificação da tabela imc, o imc do usuário, gênero do usuário e o endereço url onde API buscou o nome.
     */
    @PostMapping("/imcGender")
    public String processImcGenderRequest(@RequestParam("peso") double peso,
            @RequestParam("altura") double altura,
            @RequestParam("nome") String nome,
            Model model) {

        ImcGenderResult resultado = proccessImcGender(peso, altura, nome);

        model.addAttribute("genero", getGeneroPt(resultado.getGender()));
        model.addAttribute("imc", resultado.getImc());
        model.addAttribute("resultado", resultado.getClassification());
        model.addAttribute("url", resultado.getRequestUrl());
        return "imcGenderForm";
    }

    /**
     * 
     * Método que traduz os gêneros do usário do inglês para o português
     * 
     * @param gender Gênero do usuário.
     * @return  Retornará o gênero do usuário ou gênero indefinido traduzido em português.
     */
    public String getGeneroPt(String gender) {
        if (gender.equals("male")) {
            return "Masculino";
        } else if (gender.equals("female")) {
            return "Feminino";
        } else {
            return "Indefinido";
        }
    }


    /**
     * 
     * Método que cálcula a divisão de peso pelo produto da altura com altura.
     * 
     * @param peso  Peso do usuário
     * @param altura    Altura do usuário
     * @return  Retornará o Imc
     */
    public double getIMC(double peso, double altura) {
        return peso / (altura * altura);
    }

    /**
     * 
     * Método que classifica o estado físico do usuário de acordo com seu Imc
     * 
     * @param imc   Imc do usuário.
     * @return  Retornará uma descrição sobre o estado físico do usuário.
     */
    public String getIMCClassification(double imc) {
        if (imc < 18.8) {
            return "Abaixo do peso";
        } else if (imc < 25) {
            return "Peso normal";
        } else if (imc < 29.9) {
            return "Excesso de peso";
        } else if (imc < 34.9) {
            return "Obesidade classe I";
        } else if (imc < 39.9) {
            return "Obesidade classe II";
        } else {
            return "Obesidade classe III";
        }
    }

    /**
     * 
     * Método classifica o estado físico do usuário de acordo com seu Imc e filtra essa classificação de acordo com gênero do usuário.
     * 
     * @param imc   Imc do usuário.
     * @param gender    Gênero do usuário.
     * @return  Retornará uma descrição sobre o estado físico do usuário.
     */
    public String getIMCClassificationByGender(double imc, String gender) {
        double thresholdAbaixoPeso = 20;
        double thresholdNormal = 24.9;
        double thresholdObesidadeLeve = 29.9;
        double thresholdObesidadeModerada = 39.9;
        if (gender.equals("female")) {
            thresholdAbaixoPeso -= 1;
            thresholdNormal -= 1;
            thresholdObesidadeLeve -= 1;
            thresholdObesidadeModerada -= 1;
        }

        if (imc < thresholdAbaixoPeso) {
            return "Abaixo do peso";
        } else if (imc <= thresholdNormal) {
            return "Peso normal";
        } else if (imc <= thresholdObesidadeLeve) {
            return "Obesidade leve";
        } else if (imc <= thresholdObesidadeModerada) {
            return "Obesidade moderada";
        } else {
            return "Obesidade mórbida";
        }
    }
}
